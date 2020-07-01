/*  Calculate charging statistics. For every period of uninterrupted connection with the car,
    calculate the average price during charging and the average price if charging had occured for
    the same total time directly after the car was connected. */
CREATE OR REPLACE VIEW statistics_update AS
WITH

/*  select all rows as new as or newer than the last statistics entry and add columns with the
    previous connection state, the previous time and the next time */
su_chargelog_laglead AS (
    SELECT
        logtime, carconnected, charging, chargingallowed, price,
        LAG(carconnected) OVER (ORDER BY logtime) AS carconnected_prev,
        LAG(logtime) OVER (ORDER BY logtime) AS logtime_prev,
        LEAD(logtime) OVER (ORDER BY logtime) AS logtime_next
    FROM chargelog
    WHERE COALESCE(logtime >= (SELECT MAX(period_start) AS period_start FROM statistics), TRUE)
),

/*  number periods; a new period begins when the connection state changes or when more than ten
    minutes have elapsed since the last log entry; also calculate the time length of a log entry; if
    that is longer than 600 seconds, only count 60 seconds */
su_chargelog_periods AS (
    SELECT
        logtime, carconnected, charging, chargingallowed, price,
        SUM(CASE
                WHEN carconnected <> carconnected_prev
                OR DATEDIFF(SECOND, logtime_prev, logtime) > 600
                THEN 1 ELSE 0 END)
            OVER (ORDER BY logtime RANGE UNBOUNDED PRECEDING) AS period,
        CASE WHEN DATEDIFF(MILLISECOND, logtime, logtime_next) > 600000 THEN 60000
            ELSE DATEDIFF(MILLISECOND, logtime, logtime_next) END AS millis
    FROM su_chargelog_laglead
), 

-- calculate running time sum and overall charging time over periods
su_chargelog_sums AS (
    SELECT
        period, logtime, charging, price, millis,
        SUM(millis) OVER period_prev AS current_period_millis,
        SUM(CASE WHEN charging THEN millis ELSE 0 END) OVER period_all AS charge_millis
    FROM su_chargelog_periods
    WHERE carconnected
    WINDOW period_all AS (PARTITION BY period),
           period_prev AS (PARTITION BY period ORDER BY logtime RANGE UNBOUNDED PRECEDING)
), 

-- calculate sums over periods
su_period_sums AS (
    SELECT
        period,
        MIN(logtime) AS period_start,
        SUM(millis) AS period_millis,
        SUM(CASE WHEN charging THEN price*millis ELSE 0 END) AS pricesum,
        SUM(CASE WHEN charging THEN millis ELSE 0 END) AS charge_millis,
        SUM(CASE WHEN current_period_millis <= charge_millis THEN price*millis ELSE 0 END)
            AS unopt_pricesum,
        SUM(CASE WHEN current_period_millis <= charge_millis THEN millis ELSE 0 END) AS unopt_millis
    FROM chargelog_sums
    GROUP BY period
)

-- calculate averages
    SELECT
        period_start,
        DATEADD(MILLISECOND, period_millis, period_start) AS period_end,
        period_millis/3.6e6 AS period_hours,
        charge_millis/3.6e6 AS charge_hours,
        pricesum / charge_millis AS avg_price,
        unopt_pricesum / unopt_millis AS avg_price_unopt
    FROM su_period_sums
    WHERE charge_millis > 0
