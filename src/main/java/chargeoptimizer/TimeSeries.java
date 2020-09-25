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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Evenly-spaced time series.
 * @param <Item> class for the items that make up the time series
 */
public class TimeSeries<Item extends Object> {
    
    private final LocalDateTime start;
    private final Duration granularity;
    private final ArrayList<Item> items;
    private final Item itemBefore;
    private final Item itemAfter;
    
    public static class Entry<Item extends Object> {
        public final LocalDateTime time;
        public final Item item;
        
        public Entry(LocalDateTime time, Item item) {
            this.time = time;
            this.item = item;
        }
        
        @Override
        public String toString() {
            return "[" + time + ", " + item + "]";
        }
    }
    
    /**
     * The time series begins at time {@code start} and contains the values contained in
     * {@code items}, each of which lasts for the time {@code granularity}. Before {@code start},
     * {@code itemBefore} is returned, after {@code items.size()} times {@code granularity}
     * itemAfter is returned.
     * @param start
     * @param granularity
     * @param items
     * @param itemBefore
     * @param itemAfter 
     */
    public TimeSeries(LocalDateTime start, Duration granularity, List<Item> items, Item itemBefore,
            Item itemAfter) {
        this.start = start;
        this.granularity = granularity;
        this.items = new ArrayList<>(items);
        this.itemBefore = itemBefore;
        this.itemAfter = itemAfter;
    }
    
    /**
     * The time series begins at time {@code start} and contains the values contained in
     * {@code items}, each of which lasts for the time {@code granularity}. Before {@code start}
     * and after {@code items.size()} times {@code granularity}, {@code null} is returned.
     * @param start
     * @param granularity
     * @param items 
     */
    public TimeSeries(LocalDateTime start, Duration granularity, List<Item> items) {
        this(start, granularity, items, null, null);
    }
    
    /**
     * Build a new time series from a function. The time {@code end} is not included.
     * @param start
     * @param granularity
     * @param end
     * @param items
     * @param itemBefore
     * @param itemAfter 
     */
    public TimeSeries(LocalDateTime start, Duration granularity, LocalDateTime end,
            Function<LocalDateTime, Item> items, Item itemBefore, Item itemAfter) {
        this.start = start;
        this.granularity = granularity;
        
        this.items = new ArrayList<>();
        for (LocalDateTime time = start; time.isBefore(end); time = time.plus(granularity)) {
            this.items.add(items.apply(time));
        }
        
        this.itemBefore = itemBefore;
        this.itemAfter = itemAfter;
    }
    
    /**
     * Build a new time series from a function. The time {@code end} is not included.
     * @param start
     * @param granularity
     * @param end
     * @param items
     */
    public TimeSeries(LocalDateTime start, Duration granularity, LocalDateTime end,
            Function<LocalDateTime, Item> items) {
        this(start, granularity, end, items, null, null);
    }
    
    /**
     * Build a new time series with all null values replaced.
     * @param replacementItem the item with which null values are replaced
     * @return a new TimeSeries object with values replaced
     */
    public TimeSeries<Item> replaceNullsWith(Item replacementItem) {
        ArrayList<Item> newItems = new ArrayList(items.size());
        for (Item i : items)
            newItems.add((i == null) ? replacementItem : i);
        return new TimeSeries(start, granularity, newItems, itemBefore, itemAfter);
    }

    /**
     * Get the value at a certain time. Times between sampling points are possible; the value is
     * assumed to be constant between these points.
     * @param time
     * @return the item at this time
     */
    public Item getValueAt(LocalDateTime time) {
        int pos = (int) Duration.between(start, time).dividedBy(granularity);
        if (pos < 0) {
            return itemBefore;
        } else if (pos >= items.size()) {
            return itemAfter;
        } else {
            return items.get(pos);
        }
    }
    
    /**
     * Get the start time of this time series.
     * @return the start time
     */
    public LocalDateTime getStart() {
        return start;
    }
    
    /**
     * Get the end time of this time series.
     * @return the end time
     */
    public LocalDateTime getEnd() {
        return start.plus(granularity.multipliedBy(items.size()));
    }
    
    /**
     * @return the granularity
     */
    public Duration getGranularity() {
        return granularity;
    }
    
    /**
     * Get a list of all times contained in this time series.
     * @return an {@code ArrayList} of times starting with {@code getStart()} and ending one
     * {@code getGranularity()} before {@code getEnd()}
     */
    public ArrayList<LocalDateTime> getTimes() {
        ArrayList<LocalDateTime> times = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++)
            times.add(start.plus(granularity.multipliedBy(i)));
        return times;
    }
    
    /**
     * Get a list of all items in this time series.
     * @return the item list
     */
    public ArrayList<Item> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * Get a list of all times and items in this time series.
     * @return the list of entries
     */
    public ArrayList<Entry<Item>> getEntries() {
        ArrayList<Entry<Item>> entries = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            entries.add(new Entry(start.plus(granularity.multipliedBy(i)), items.get(i)));
        }
        return entries;
    }
    
    /**
     * Get the number of entries in this time series.
     * @return 
     */
    public int size() {
        return items.size();
    }

}
