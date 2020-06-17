/*
 * Copyright (C) 2020 t-pa <t-pa@posteo.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package chargeoptimizer;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/**
 * This is the main class that connects a {@code Charger}, a {@code CostSource}, an
 * {@code Optimizer} and the {@code StatisticsDatabase} in order to optimize the charging times
 * of your car.
 * 
 * All times are {@code LocalDateTime}s and implicitly refer to the UTC time zone.
 * 
 * When the {@code ChargeOptimizer} has been {@code start()}ed, all accesses to this class with
 * the exception of {@code stop()} and {@code getXXX()} should be done via its
 * {@code executorService}.
 */
public class ChargeOptimizer {
    
    final org.slf4j.Logger logger = LoggerFactory.getLogger(ChargeOptimizer.class);
    
    private CostSource costSource;
    private Optimizer optimizer;
    private Charger charger;
    private StatisticsDatabase statisticsDatabase;
    
    private Duration checkInterval = Duration.ofSeconds(5);
    private Duration logInterval = Duration.ofSeconds(60);
    private Duration optimizationTime = Duration.ofHours(8);
    private Duration granularity = Duration.ofMinutes(5);
    
    private ScheduledExecutorService executorService;
    
    private Charger.State chargerState = Charger.State.NO_CAR;
    private boolean chargerEnabled = false;
    private boolean override = false;
    private TimeSeries<Double> optimCosts;
    private TimeSeries<Boolean> optimResult;
    private LocalDateTime lastStateChange = TimeUtils.now();
    private LocalDateTime lastEnabledChange = TimeUtils.now();

    private void checkState() {
        if (charger == null)
            return;
        
        try {
            Charger.State previousState = chargerState;
            chargerState = charger.getState();
            if (chargerState != previousState)
                lastStateChange = TimeUtils.now();
            
            if (chargerState == Charger.State.ERROR) {
                logger.error("Charger in state ERROR.");
                return;                
            }
            
            // check for car connection state
            if (!previousState.isConnected() && chargerState.isConnected()) {
                logger.info("Car connected.");
                // run optimization
                if (optimizer != null && costSource != null) {
                    LocalDateTime start = TimeUtils.roundTimeTo(TimeUtils.now(), granularity);
                    optimCosts = new TimeSeries<>(start, granularity, start.plus(optimizationTime),
                            costSource::getCostAt);
                    optimResult = optimizer.optimize(optimCosts);
                    
                    logger.info("Optimization result: " + optimResult.getEntries());
                }
            } else if (previousState.isConnected() && !chargerState.isConnected()) {
                logger.info("Car disconnected.");
                optimResult = null;
                chargerEnabled = false;
                override = false;
            }
            
            // check for new optimization state
            if (chargerState.isConnected() && !override && optimResult != null) {
                chargerEnabled = optimResult.getValueAt(TimeUtils.now());
            }
            
            // check for charger enabled state
            if (charger.getEnabled() != chargerEnabled) {
                logger.info("Setting charger to " + (chargerEnabled ? "enabled." : "disabled."));
                charger.setEnabled(chargerEnabled);
                
                lastEnabledChange = TimeUtils.now();
            }
        } catch (IOException ex) {
            logger.error("Connection problem with charger.", ex);
        }
    }
    
    private void logState() {
        if (statisticsDatabase == null)
            return;
        
        LocalDateTime now = TimeUtils.now().truncatedTo(ChronoUnit.SECONDS);
        Double cost = costAt(now);
        if (cost == null) cost = Double.NaN;
        
        // the charger state can be a few seconds old (at most of age checkInterval)
        statisticsDatabase.logState(now, chargerState, chargerEnabled, cost);
    }
    
    /**
     * Get cost at a certain time. If an optimization has been performed, return the cost used in
     * the optimization. Otherwise use the {@code costSource}. This method is not thread-safe and
     * should be called via the {@code executorService} once {@code start()} has been called.
     * @param time
     * @return the cost or {@code null} if it could not be determined
     */
    public Double costAt(LocalDateTime time) {
        Double cost = null;
        
        if (optimResult != null) {
            cost = optimCosts.getValueAt(time);
        }
        
        if (cost == null && costSource != null) {
            cost = costSource.getCostAt(time);
        }
        
        return cost;
    }
    
    /**
     * Get the time at which the next enabled state change after the time {@code afterTime} will
     * happen. This method is not thread-safe and should be called via the {@code executorService}
     * once {@code start()} has been called.
     * @param afterTime
     * @return the time of the next change, or null if no change is planned
     */
    public LocalDateTime nextEnabledStateChange(LocalDateTime afterTime) {
        if (optimResult != null) {
            boolean enabled = optimResult.getValueAt(afterTime);
            for (TimeSeries.Entry<Boolean> e : optimResult.getEntries()) {
                if (e.time.isAfter(afterTime) && e.item != enabled) {
                    return e.time;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Start the charge optimizer. It will regularly check on the charger state and act
     * accordingly and also write statistics to the database.
     */
    public void start() {
        logger.info("Starting...");
        executorService = Executors.newSingleThreadScheduledExecutor();
        
        executorService.scheduleWithFixedDelay(this::checkState, 0,
                checkInterval.toMillis(), TimeUnit.MILLISECONDS);
        
        // let logging intervals coincide with full days
        LocalDateTime nextLog = TimeUtils.roundTimeTo(TimeUtils.now(), logInterval)
                .plus(logInterval);
        executorService.scheduleAtFixedRate(this::logState,
                TimeUtils.now().until(nextLog, ChronoUnit.MILLIS), logInterval.toMillis(),
                TimeUnit.MILLISECONDS);
    }
    
    /**
     * Stop all activities.
     */
    public void stop() {
        if (executorService != null) {
            logger.info("Shutting down...");
            
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) { }
            
            if (!executorService.isTerminated()) {
                logger.info("Trying again...");
                executorService.shutdownNow();
                try {
                    executorService.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) { }
            }
            
            if (executorService.isTerminated()) {
                executorService = null;
                logger.info("Finished.");
            } else {
                logger.error("Could not shut down executor service.");
            }
        }
        
        // leave charger in enabled state
        try {
            if (charger != null)
                charger.setEnabled(true);
        } catch (IOException ex) {
            logger.error("Could not set charger enabled state.", ex);
        }
    }
    
// <editor-fold defaultstate="collapsed" desc="getter/setter">
    public CostSource getCostSource() {
        return costSource;
    }

    public void setCostSource(CostSource costSource) {
        this.costSource = costSource;
    }

    public Optimizer getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(Optimizer optimizer) {
        this.optimizer = optimizer;
    }

    public Charger getCharger() {
        return charger;
    }

    public void setCharger(Charger charger) {
        this.charger = charger;
    }

    public StatisticsDatabase getStatisticsDatabase() {
        return statisticsDatabase;
    }

    public void setStatisticsDatabase(StatisticsDatabase statisticsDatabase) {
        this.statisticsDatabase = statisticsDatabase;
    }
    
    public Duration getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(Duration checkInterval) {
        this.checkInterval = checkInterval;
    }

    public Duration getLogInterval() {
        return logInterval;
    }

    public void setLogInterval(Duration logInterval) {
        this.logInterval = logInterval;
    }

    public Duration getOptimizationTime() {
        return optimizationTime;
    }

    public void setOptimizationTime(Duration optimizationTime) {
        this.optimizationTime = optimizationTime;
    }

    public Duration getGranularity() {
        return granularity;
    }

    public void setGranularity(Duration granularity) {
        this.granularity = granularity;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }

    public Charger.State getChargerState() {
        return chargerState;
    }

    public boolean getChargerEnabled() {
        return chargerEnabled;
    }

    public void setChargerEnabled(boolean chargerEnabled) {
        this.chargerEnabled = chargerEnabled;
    }

    public boolean getOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }
    
    public LocalDateTime getLastStateChange() {
        return lastStateChange;
    }

    public LocalDateTime getLastEnabledChange() {
        return lastEnabledChange;
    }
// </editor-fold>

}
