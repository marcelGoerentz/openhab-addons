/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.lgessenervu.internal;

import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_BATTERY_LOW;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_BATTERY_SAFETYSOC;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_BATTERY_SOC;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_BATTERY_STATUS;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_BATTERY_WINTERMODE;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_CURRENT_DIRECT_POWER_CONSUMPTION;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_CURRENT_POWER_FROM_BATTERY;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_CURRENT_POWER_FROM_GRID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_CURRENT_POWER_FROM_PV;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_CURRENT_POWER_TO_BATTERY;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_CURRENT_POWER_TO_GRID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_CURRENT_TOTAL_POWER_CONSUMPTION;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_DAILY_DIRECT_POWER_CONSUMPTION_FROM_PV;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_DAILY_POWER_FROM_BATTERY;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_DAILY_POWER_FROM_GRID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_DAILY_POWER_FROM_PV;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_DAILY_POWER_TO_BATTERY;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_DAILY_POWER_TO_GRID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_DAILY_TOTAL_POWER_CONSUMPTION;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_ISBUYING;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_ISCHARGING;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_ISCHARGINGFROMGRID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_ISDIRECTUSE;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_ISDISCHARGING;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_ISSELLING;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_CO2SAVINGS;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_DIRECT_POWER_CONSUMPTION_FROM_PV;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_EARNINGS;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_PAID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_POWER_FROM_BATTERY;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_POWER_FROM_GRID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_POWER_FROM_PV;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_POWER_TO_BATTERY;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_POWER_TO_GRID;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_SAVINGS;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_MONTHLY_TOTAL_POWER_CONSUMPTION;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_SELFCONSUMPTION_FROM_PV;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING1_CURRENT;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING1_POWER;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING1_VOLTAGE;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING2_CURRENT;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING2_POWER;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING2_VOLTAGE;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING3_CURRENT;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING3_POWER;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING3_VOLTAGE;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING4_CURRENT;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING4_POWER;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING4_VOLTAGE;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING5_CURRENT;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING5_POWER;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_STRING5_VOLTAGE;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_TRIGGER_BATTERYSTATUS;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CHANNEL_TRIGGER_POWERPRODUCTIONABOVETHRESHOLD;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.CURRENT_GENERATED_POWER;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.EMPTYBATTERY_ALREADY_TRIGGERED;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.FULLBATTERY_ALREADY_TRIGGERED;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.GENPOWERTHRESHOLD_ALREADY_TRIGGERED;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.GENPOWERTHRESHOLD_RESET_TRIGGER;
import static org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.LOWBATTERY_ALREADY_TRIGGERED;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgessenervu.internal.LGEssEnervuBindingConstants.FailReason;
import org.openhab.binding.lgessenervu.internal.client.IResponseCallback;
import org.openhab.binding.lgessenervu.internal.client.LGCloudClient;
import org.openhab.binding.lgessenervu.internal.client.LGEssClient;
import org.openhab.binding.lgessenervu.internal.client.LGLanClient;
import org.openhab.binding.lgessenervu.internal.client.ResponseData;
import org.openhab.binding.lgessenervu.internal.job.FifteenMinJob;
import org.openhab.binding.lgessenervu.internal.job.SnapshotJob;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LGEssenervuHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Klama - Initial contribution
 */
@NonNullByDefault
public class LGEssenervuHandler extends BaseThingHandler implements IResponseCallback {

    private final Logger logger = LoggerFactory.getLogger(LGEssenervuHandler.class);

    private LGEssenervuConfiguration config;
    private final @Nullable HttpClient httpclient;

    private @Nullable CronScheduler cronscheduler;
    private @Nullable ScheduledCompletableFuture<?> fifteenminJob;
    private final Lock monitor = new ReentrantLock();
    private final Set<ScheduledFuture<?>> scheduledFutures = new HashSet<>();
    private final String CRON_15MIN = "0 0/15 * ? * * *";

    private LGEssClient lgessClient;
    private int refreshInterval;
    private double co2factor;
    private double eurprokwh;
    private double eurprokwhs;
    private int lowbatterythreshold;
    private int trigger_genpower_threshold;
    private long trigger_genpower_time;
    private long trigger_genpower_resettime;

    public LGEssenervuHandler(Thing thing, @Nullable HttpClient client, @Nullable CronScheduler cronScheduler) {
        super(thing);
        this.httpclient = client;
        this.cronscheduler = cronScheduler;
        this.config = new LGEssenervuConfiguration();
        lgessClient = new LGCloudClient(httpclient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            scheduler.execute(() -> {
                if (lgessClient.getLoginStatus()) {
                    lgessClient.getCurrentData();
                    if (true == config.dataSourceCloud) {
                        lgessClient.get15MinOverview();
                    }
                }
            });
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(LGEssenervuConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        if (true == config.dataSourceCloud) {

            if (config.passwordCloud.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "you need to provide the password for the Cloud-API");
                return;
            }
            if (config.user.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "you need to provide the username/email for the Cloud-API");
                return;
            }

            lgessClient = new LGCloudClient(httpclient);
            lgessClient.setUserID(config.user);
            lgessClient.setPassword(config.passwordCloud);
            refreshInterval = config.refreshIntervalCloud;

        } else {

            if (config.hostName.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "you need to provide the host/ip of the device! (enable advance config)");
                return;
            }
            if (config.passwordLocal.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "you need to provide the password for the LAN-API");
                return;
            }

            lgessClient = new LGLanClient(httpclient);
            lgessClient.setPassword(config.passwordLocal);
            lgessClient.setHostname(config.hostName);
            refreshInterval = config.refreshInterval;
        }

        co2factor = config.co2Factor;
        eurprokwh = config.kwhPrice;
        eurprokwhs = config.kwhPriceSell;
        lowbatterythreshold = config.lowbatterythreshold;

        trigger_genpower_threshold = config.triggergeneratedpowerthreshold;
        trigger_genpower_time = config.triggergeneratedpowerthresholdtime;
        trigger_genpower_resettime = config.triggergeneratedpowerthresholdresettime;

        lgessClient.setTimeout(config.timeout);
        lgessClient.registerCallback(this);

        scheduler.execute(() -> {
            if (false == lgessClient.getLoginStatus()) {
                try {
                    lgessClient.Login();
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Wrong credentials");
                }
            }
        });

        logger.info("LGEssEnervu Thing initialized");
    }

    @Override
    public void dispose() {
        lgessClient.unregisterCallback();
        stopPolling();
        super.dispose();
    }

    @Override
    public void handleRemoval() {
        lgessClient.unregisterCallback();
        stopPolling();
        super.handleRemoval();
    }

    public void startPolling() {
        monitor.lock();
        try {
            SnapshotJob sjob = new SnapshotJob(lgessClient);

            ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(sjob, 0, refreshInterval, TimeUnit.SECONDS);
            scheduledFutures.add(future);

            if (true == getConfigAs(LGEssenervuConfiguration.class).dataSourceCloud) {

                FifteenMinJob runnable = new FifteenMinJob(lgessClient);
                if (null != cronscheduler) {
                    fifteenminJob = cronscheduler.schedule(runnable, CRON_15MIN);
                    runnable.run();
                }
            }
        } catch (Exception ex) {
            logger.error("startpolling - refreshInterval {} with error {}", refreshInterval, ex.getMessage(), ex);
        } finally {
            monitor.unlock();
        }
    }

    public void stopPolling() {
        monitor.lock();
        try {
            if (cronscheduler != null) {
                if (fifteenminJob != null) {
                    fifteenminJob.cancel(true);
                }
                fifteenminJob = null;
            }

            for (ScheduledFuture<?> future : scheduledFutures) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
            scheduledFutures.clear();
        } catch (Exception e) {
            logger.error("Failed to stop polling jobs!");
        } finally {
            monitor.unlock();
        }
    }

    @Override
    public void responseCallbackLoggedIn(boolean isloggedin, FailReason reason) {

        if (false == isloggedin) {
            stopPolling();

            switch (reason) {
                case COMMUNICATION_ERROR:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Communication with service failed :(");
                    break;
                case NONE:
                    updateStatus(ThingStatus.OFFLINE);
                    break;
                case WRONG_CREDENTIALS:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Access denied. Check credentials!");
                    return;
                default:
                    updateStatus(ThingStatus.OFFLINE);
                    break;

            }

            scheduler.schedule(() -> {
                if (false == lgessClient.getLoginStatus()) {
                    lgessClient.Login();

                }
            }, LGEssEnervuBindingConstants.RECONNECT_DELAY, TimeUnit.SECONDS);

        } else {
            updateStatus(ThingStatus.ONLINE);

            if (scheduledFutures.isEmpty()) {
                startPolling();
            }
        }
    }

    public void publishChannelIfLinked(String channelId, Object value) {
        final Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            // logger.warn("Cannot find channel for {} of thing {}", channelId, getThing().getUID());
            return;
        }

        if (isLinked(channel.getUID().getId()) && getThing().isEnabled()
                && (getThing().getStatus() == ThingStatus.ONLINE)) {

            try {
                if (value instanceof String) {
                    updateState(channel.getUID(), new StringType((String) value));
                } else if (value instanceof Integer) {
                    updateState(channel.getUID(), new DecimalType((int) value));
                } else if (value instanceof QuantityType) {
                    updateState(channel.getUID(), (QuantityType<?>) value);
                } else if (value instanceof OnOffType) {
                    updateState(channel.getUID(), (OnOffType) value);
                } else if (value instanceof Double) {
                    updateState(channel.getUID(), new DecimalType((double) value));
                } else {
                    logger.error("unable to publish {} since type is {}", channel.getUID(), value.getClass());
                }

            } catch (Exception ex) {
                logger.error("Can't update state for channel {} : {}", channel.getUID(), ex.getMessage(), ex);
            }
        }
    }

    private double getDoubleValueOfString(String input) {
        double returnval = 0;

        try {
            returnval = Double.parseDouble(input);
        } catch (Exception e) {
            logger.error("getDoubleValueOfString {}", e.getMessage(), e);
        }

        return returnval;
    }

    private int getNumericValueOfString(String input) {
        int returnval = 0;

        if (!input.equals("")) {
            try {
                returnval = (int) Double.parseDouble(input);
            } catch (Exception e) {
                logger.error("getNumericValueOfString {}", e.getMessage(), e);
            }
        }

        return returnval;
    }

    /**
     * Emits an event for the given channel.
     */
    public void triggerEvent(String channelId, String event) {
        final Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.warn("Event {} in thing {} does not exist, please recreate the thing", event, getThing().getUID());
            return;
        }
        triggerChannel(channel.getUID(), event);
    }

    @Override
    public void responseCallbackCurrentData(ResponseData responseData) {

        if (this.getThing().getStatus() != ThingStatus.ONLINE) {
            stopPolling();
            return;
        }

        if (responseData.getStats().getDirection().getIsGridBuying().equals("1")) {
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_FROM_GRID, new QuantityType<>(
                    getNumericValueOfString(responseData.getCommon().getGRID().getActivePower()), Units.WATT));
        } else {
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_FROM_GRID, new QuantityType<>(0.0, Units.WATT));
        }

        if (responseData.getStats().getDirection().getIsGridSelling().equals("1")) {
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_TO_GRID, new QuantityType<>(
                    getNumericValueOfString(responseData.getCommon().getGRID().getActivePower()), Units.WATT));
        } else {
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_TO_GRID, new QuantityType<>(0.0, Units.WATT));
        }

        publishChannelIfLinked(CHANNEL_STRING1_VOLTAGE, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv1Voltage()), Units.VOLT));

        publishChannelIfLinked(CHANNEL_STRING2_VOLTAGE, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv2Voltage()), Units.VOLT));

        publishChannelIfLinked(CHANNEL_STRING3_VOLTAGE, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv3Voltage()), Units.VOLT));

        publishChannelIfLinked(CHANNEL_STRING4_VOLTAGE, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv4Voltage()), Units.VOLT));

        publishChannelIfLinked(CHANNEL_STRING5_VOLTAGE, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv5Voltage()), Units.VOLT));

        publishChannelIfLinked(CHANNEL_STRING1_CURRENT, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv1Current()), Units.AMPERE));

        publishChannelIfLinked(CHANNEL_STRING2_CURRENT, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv2Current()), Units.AMPERE));

        publishChannelIfLinked(CHANNEL_STRING3_CURRENT, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv3Current()), Units.AMPERE));

        publishChannelIfLinked(CHANNEL_STRING4_CURRENT, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv4Current()), Units.AMPERE));

        publishChannelIfLinked(CHANNEL_STRING5_CURRENT, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv5Current()), Units.AMPERE));

        publishChannelIfLinked(CHANNEL_STRING1_POWER, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv1Power()), Units.WATT));

        publishChannelIfLinked(CHANNEL_STRING2_POWER, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv2Power()), Units.WATT));

        publishChannelIfLinked(CHANNEL_STRING3_POWER, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv3Power()), Units.WATT));

        publishChannelIfLinked(CHANNEL_STRING4_POWER, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv4Power()), Units.WATT));

        publishChannelIfLinked(CHANNEL_STRING5_POWER, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getPv5Power()), Units.WATT));

        // take all 5 strings into account
        int allstrings = getNumericValueOfString(responseData.getCommon().getPV().getPv1Power());
        allstrings += getNumericValueOfString(responseData.getCommon().getPV().getPv2Power());
        allstrings += getNumericValueOfString(responseData.getCommon().getPV().getPv3Power());
        allstrings += getNumericValueOfString(responseData.getCommon().getPV().getPv4Power());
        allstrings += getNumericValueOfString(responseData.getCommon().getPV().getPv5Power());

        CURRENT_GENERATED_POWER = allstrings;

        publishChannelIfLinked(CHANNEL_CURRENT_POWER_FROM_PV, new QuantityType<>(allstrings, Units.WATT));

        if (trigger_genpower_threshold >= allstrings && false == GENPOWERTHRESHOLD_ALREADY_TRIGGERED) {
            GENPOWERTHRESHOLD_ALREADY_TRIGGERED = true;

            if (trigger_genpower_time == 0) {
                triggerEvent(CHANNEL_TRIGGER_POWERPRODUCTIONABOVETHRESHOLD, "POWER");
            } else {
                scheduler.schedule(() -> {
                    if (CURRENT_GENERATED_POWER >= trigger_genpower_threshold) {
                        triggerEvent(CHANNEL_TRIGGER_POWERPRODUCTIONABOVETHRESHOLD, "POWER");
                    }
                }, trigger_genpower_time, TimeUnit.SECONDS);
            }
        } else {
            if (CURRENT_GENERATED_POWER < trigger_genpower_threshold && true == GENPOWERTHRESHOLD_ALREADY_TRIGGERED) {
                if (false == GENPOWERTHRESHOLD_RESET_TRIGGER) {
                    GENPOWERTHRESHOLD_RESET_TRIGGER = true;
                    scheduler.schedule(() -> {
                        if (CURRENT_GENERATED_POWER <= trigger_genpower_threshold) {
                            GENPOWERTHRESHOLD_ALREADY_TRIGGERED = false;
                            GENPOWERTHRESHOLD_RESET_TRIGGER = false;
                        }
                    }, trigger_genpower_resettime, TimeUnit.SECONDS);
                }
            }
        }

        publishChannelIfLinked(CHANNEL_SELFCONSUMPTION_FROM_PV, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPCS().getTodaySelfConsumption()), Units.PERCENT));

        // SOC STATE
        double socstate = getDoubleValueOfString(responseData.getCommon().getBATT().getSoc());

        publishChannelIfLinked(CHANNEL_BATTERY_SOC,
                new QuantityType<>(getDoubleValueOfString(responseData.getCommon().getBATT().getSoc()), Units.PERCENT));

        if (socstate <= 0.1 && false == EMPTYBATTERY_ALREADY_TRIGGERED) {
            triggerEvent(CHANNEL_TRIGGER_BATTERYSTATUS, "DEPLETED");
            EMPTYBATTERY_ALREADY_TRIGGERED = true;
        } else if (socstate > 1.0) {
            EMPTYBATTERY_ALREADY_TRIGGERED = false;
        }

        // battery soc < defined threshold in config (default 20% soc
        if (socstate <= lowbatterythreshold && false == LOWBATTERY_ALREADY_TRIGGERED) {
            publishChannelIfLinked(CHANNEL_BATTERY_LOW, OnOffType.from("ON")); // for homekit
            triggerEvent(CHANNEL_TRIGGER_BATTERYSTATUS, "LOWBATTERY");
            LOWBATTERY_ALREADY_TRIGGERED = true;
        }

        if (socstate >= lowbatterythreshold && true == LOWBATTERY_ALREADY_TRIGGERED) {
            publishChannelIfLinked(CHANNEL_BATTERY_LOW, OnOffType.from("OFF")); //
            LOWBATTERY_ALREADY_TRIGGERED = false;
        }

        if (socstate >= 99.9 && false == FULLBATTERY_ALREADY_TRIGGERED) {
            triggerEvent(CHANNEL_TRIGGER_BATTERYSTATUS, "CHARGED");
            FULLBATTERY_ALREADY_TRIGGERED = true;
        } else if (socstate < 99) {
            FULLBATTERY_ALREADY_TRIGGERED = false;
        }
        // end SOC state

        publishChannelIfLinked(CHANNEL_BATTERY_SAFETYSOC, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getBATT().getSaftySoc()), Units.PERCENT));

        publishChannelIfLinked(CHANNEL_BATTERY_STATUS, responseData.getCommon().getBATT().getStatus());

        publishChannelIfLinked(CHANNEL_BATTERY_WINTERMODE,
                OnOffType.from(responseData.getCommon().getBATT().getWinterStatus())); //

        publishChannelIfLinked(CHANNEL_CURRENT_TOTAL_POWER_CONSUMPTION, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getLOAD().getLoadPower()), Units.WATT));

        // calculated channels

        double directpow = 0;
        try {
            directpow = Double.parseDouble(responseData.getCommon().getLOAD().getLoadPower())
                    - Double.parseDouble(responseData.getCommon().getGRID().getActivePower());

            if (directpow < 0) {
                directpow = 0;
            }
        } catch (Exception e) {

        }
        publishChannelIfLinked(CHANNEL_CURRENT_DIRECT_POWER_CONSUMPTION, new QuantityType<>(directpow, Units.WATT));

        if (responseData.getStats().getDirection().getIsBatteryCharging().equals("0")
                && responseData.getStats().getDirection().getIsBatteryDischarging().equals("0")) {
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_FROM_BATTERY, new QuantityType<>(0.0, Units.WATT));
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_TO_BATTERY, new QuantityType<>(0.0, Units.WATT));
        }

        if (responseData.getStats().getDirection().getIsBatteryCharging().equals("1")) {
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_FROM_BATTERY, new QuantityType<>(0.0, Units.WATT));
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_TO_BATTERY, new QuantityType<>(
                    getNumericValueOfString(responseData.getCommon().getBATT().getDcPower()), Units.WATT));
        }

        if (responseData.getStats().getDirection().getIsBatteryDischarging().equals("1")) {
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_FROM_BATTERY, new QuantityType<>(
                    getNumericValueOfString(responseData.getCommon().getBATT().getDcPower()), Units.WATT));
            publishChannelIfLinked(CHANNEL_CURRENT_POWER_TO_BATTERY, new QuantityType<>(0.0, Units.WATT));
        }

        publishChannelIfLinked(CHANNEL_ISDIRECTUSE,
                OnOffType.from(responseData.getCommon().getBATT().getWinterStatus()));

        publishChannelIfLinked(CHANNEL_ISCHARGING,
                OnOffType.from(responseData.getStats().getDirection().getIsBatteryCharging()));

        publishChannelIfLinked(CHANNEL_ISDISCHARGING,
                OnOffType.from(responseData.getStats().getDirection().getIsBatteryDischarging()));

        publishChannelIfLinked(CHANNEL_ISSELLING,
                OnOffType.from(responseData.getStats().getDirection().getIsGridSelling()));

        publishChannelIfLinked(CHANNEL_ISBUYING,
                OnOffType.from(responseData.getStats().getDirection().getIsGridBuying()));

        publishChannelIfLinked(CHANNEL_ISCHARGINGFROMGRID,
                OnOffType.from(responseData.getStats().getDirection().getIsChargingFromGrid()));
    }

    @Override
    public void responseCallbackDaily(ResponseData responseData) {
        if (this.getThing().getStatus() != ThingStatus.ONLINE) {
            stopPolling();
            return;
        }

        publishChannelIfLinked(CHANNEL_DAILY_POWER_FROM_GRID,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getGRID().getTodayGridPowerPurchaseEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_POWER_FROM_GRID,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getGRID().getMonthGridPowerPurchaseEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_DAILY_POWER_FROM_PV, new QuantityType<>(
                getNumericValueOfString(responseData.getCommon().getPV().getTodayPvGenerationSum()), Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_DAILY_POWER_TO_GRID,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getGRID().getTodayGridFeedInEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_POWER_FROM_PV,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getPV().getTodayMonthPvGenerationSum()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_POWER_TO_GRID,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getGRID().getMonthGridFeedInEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_DAILY_POWER_TO_BATTERY,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getBATT().getTodayBattChargeEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_POWER_TO_BATTERY,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getBATT().getMonthBattChargeEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_DAILY_POWER_FROM_BATTERY,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getBATT().getTodayBattDischargeEnery()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_POWER_FROM_BATTERY,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getBATT().getMonthBattDischargeEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_DAILY_TOTAL_POWER_CONSUMPTION,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getLOAD().getTodayLoadConsumptionSum()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_TOTAL_POWER_CONSUMPTION,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getLOAD().getMonthLoadConsumptionSum()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_DAILY_DIRECT_POWER_CONSUMPTION_FROM_PV,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getLOAD().getTodayPvDirectConsumptionEnegy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_DIRECT_POWER_CONSUMPTION_FROM_PV,
                new QuantityType<>(
                        getNumericValueOfString(responseData.getCommon().getLOAD().getMonthPvDirectConsumptionEnergy()),
                        Units.WATT_HOUR));

        publishChannelIfLinked(CHANNEL_MONTHLY_CO2SAVINGS, new QuantityType<>(
                ((getNumericValueOfString(responseData.getCommon().getPV().getTodayMonthPvGenerationSum())) * co2factor)
                        / 1000,
                SIUnits.KILOGRAM));

        double buy = getNumericValueOfString(responseData.getCommon().getGRID().getMonthGridPowerPurchaseEnergy());
        // given in Wh... need it in kWh
        buy = buy / 1000;
        buy = buy * eurprokwh;

        publishChannelIfLinked(CHANNEL_MONTHLY_PAID, buy);

        double sell = getNumericValueOfString(responseData.getCommon().getGRID().getMonthGridFeedInEnergy());
        // given in Wh... need it in kWh
        sell = sell / 1000;
        sell = sell * eurprokwhs;
        publishChannelIfLinked(CHANNEL_MONTHLY_EARNINGS, sell);

        double savings = getNumericValueOfString(responseData.getCommon().getLOAD().getMonthLoadConsumptionSum())
                - getNumericValueOfString(responseData.getCommon().getLOAD().getMonthGridPowerPurchaseEnergy());
        // given in Wh... need it in kWh
        savings = savings / 1000.00;
        savings = savings * eurprokwh;
        publishChannelIfLinked(CHANNEL_MONTHLY_SAVINGS, savings);
    }

    @Override
    public void responseCallbackError(FailReason reason) {
        switch (reason) {
            case COMMUNICATION_ERROR:
                stopPolling();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Check network connection please.");
                break;
            case CRITICAL:
                stopPolling();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Critical error - File a bug report please!");
                break;
            case NONE:
                break;
            case WRONG_CREDENTIALS:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Login failed. please check your credentials!");
                break;
            default:
                break;

        }
    }
}
