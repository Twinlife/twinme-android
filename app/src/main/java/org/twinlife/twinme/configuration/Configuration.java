/*
 *  Copyright (c) 2014-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 */

package org.twinlife.twinme.configuration;

import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.twinlife.AccountService.AuthenticationAuthority;
import org.twinlife.twinlife.Serializer;
import org.twinlife.twinme.TwinmeConfiguration;
import org.twinlife.twinme.models.Message;
import org.twinlife.twinme.models.RoomCommand;
import org.twinlife.twinme.models.RoomCommandResult;
import org.twinlife.twinme.models.RoomConfigResult;
import org.twinlife.twinme.models.Typing;
import org.twinlife.twinme.ui.TwinmeApplicationImpl;

public class Configuration extends TwinmeConfiguration {

    private static final String APPLICATION_NAME = "skred";

    public Configuration() {
        super(true);

        applicationName = APPLICATION_NAME;
        applicationVersion = BuildConfig.VERSION_NAME;
        serializers = new Serializer[5];
        serializers[0] = new Message.MessageSerializer();
        serializers[1] = new Typing.TypingSerializer();
        serializers[2] = new RoomCommand.RoomCommandSerializer();
        serializers[3] = new RoomCommandResult.RoomResultSerializer();
        serializers[4] = new RoomConfigResult.RoomConfigResultSerializer();

        accountServiceConfiguration.defaultAuthenticationAuthority = AuthenticationAuthority.DEVICE;

        conversationServiceConfiguration.serviceOn = true;

        notificationServiceConfiguration.serviceOn = true;

        peerConnectionServiceConfiguration.serviceOn = true;
        peerConnectionServiceConfiguration.acceptIncomingCalls = true;

        repositoryServiceConfiguration.serviceOn = true;

        twincodeFactoryServiceConfiguration.serviceOn = true;

        twincodeInboundServiceConfiguration.serviceOn = true;

        twincodeOutboundServiceConfiguration.serviceOn = true;

        imageServiceConfiguration.serviceOn = true;

        accountMigrationServiceConfiguration.serviceOn = true;

        peerCallServiceConfiguration.serviceOn = true;
        cryptoServiceConfiguration.serviceOn = true;
    }
}
