/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common

import com.cobblemon.mod.common.api.net.ClientNetworkPacketHandler
import com.cobblemon.mod.common.api.net.NetworkPacket
import com.cobblemon.mod.common.api.net.ServerNetworkPacketHandler
import com.cobblemon.mod.common.client.net.PlayerInteractOptionsHandler
import com.cobblemon.mod.common.client.net.SetClientPlayerDataHandler
import com.cobblemon.mod.common.client.net.animation.PlayPosableAnimationHandler
import com.cobblemon.mod.common.client.net.battle.*
import com.cobblemon.mod.common.client.net.callback.move.OpenMoveCallbackHandler
import com.cobblemon.mod.common.client.net.callback.party.OpenPartyCallbackHandler
import com.cobblemon.mod.common.client.net.callback.partymove.OpenPartyMoveCallbackHandler
import com.cobblemon.mod.common.client.net.data.DataRegistrySyncPacketHandler
import com.cobblemon.mod.common.client.net.data.UnlockReloadPacketHandler
import com.cobblemon.mod.common.client.net.effect.RunPosableMoLangHandler
import com.cobblemon.mod.common.client.net.dialogue.DialogueClosedHandler
import com.cobblemon.mod.common.client.net.dialogue.DialogueOpenedHandler
import com.cobblemon.mod.common.client.net.effect.SpawnSnowstormEntityParticleHandler
import com.cobblemon.mod.common.client.net.effect.SpawnSnowstormParticleHandler
import com.cobblemon.mod.common.client.net.gui.InteractPokemonUIPacketHandler
import com.cobblemon.mod.common.client.net.gui.SummaryUIPacketHandler
import com.cobblemon.mod.common.client.net.pasture.ClosePastureHandler
import com.cobblemon.mod.common.client.net.pasture.OpenPastureHandler
import com.cobblemon.mod.common.client.net.pasture.PokemonPasturedHandler
import com.cobblemon.mod.common.client.net.pasture.PokemonUnpasturedHandler
import com.cobblemon.mod.common.client.net.pokemon.update.PokemonUpdatePacketHandler
import com.cobblemon.mod.common.client.net.settings.ServerSettingsPacketHandler
import com.cobblemon.mod.common.client.net.sound.UnvalidatedPlaySoundS2CPacketHandler
import com.cobblemon.mod.common.client.net.spawn.SpawnExtraDataEntityHandler
import com.cobblemon.mod.common.client.net.starter.StarterUIPacketHandler
import com.cobblemon.mod.common.client.net.storage.RemoveClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.SwapClientPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.InitializePartyHandler
import com.cobblemon.mod.common.client.net.storage.party.MoveClientPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyPokemonHandler
import com.cobblemon.mod.common.client.net.storage.party.SetPartyReferenceHandler
import com.cobblemon.mod.common.client.net.storage.pc.ClosePCHandler
import com.cobblemon.mod.common.client.net.storage.pc.InitializePCHandler
import com.cobblemon.mod.common.client.net.storage.pc.MoveClientPCPokemonHandler
import com.cobblemon.mod.common.client.net.storage.pc.OpenPCHandler
import com.cobblemon.mod.common.client.net.storage.pc.SetPCBoxPokemonHandler
import com.cobblemon.mod.common.client.net.storage.pc.SetPCPokemonHandler
import com.cobblemon.mod.common.client.net.toast.ToastPacketHandler
import com.cobblemon.mod.common.client.net.trade.TradeAcceptanceChangedHandler
import com.cobblemon.mod.common.client.net.trade.TradeCancelledHandler
import com.cobblemon.mod.common.client.net.trade.TradeCompletedHandler
import com.cobblemon.mod.common.client.net.trade.TradeOfferExpiredHandler
import com.cobblemon.mod.common.client.net.trade.TradeOfferNotificationHandler
import com.cobblemon.mod.common.client.net.trade.TradeStartedHandler
import com.cobblemon.mod.common.client.net.trade.TradeUpdatedHandler
import com.cobblemon.mod.common.net.messages.client.PlayerInteractOptionsPacket
import com.cobblemon.mod.common.net.messages.client.animation.PlayPosableAnimationPacket
import com.cobblemon.mod.common.net.messages.client.battle.*
import com.cobblemon.mod.common.net.messages.client.callback.OpenMoveCallbackPacket
import com.cobblemon.mod.common.net.messages.client.callback.OpenPartyCallbackPacket
import com.cobblemon.mod.common.net.messages.client.callback.OpenPartyMoveCallbackPacket
import com.cobblemon.mod.common.net.messages.client.data.AbilityRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.BerryRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.GlobalSpeciesFeatureSyncPacket
import com.cobblemon.mod.common.net.messages.client.data.MovesRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.PropertiesCompletionRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.SpeciesFeatureAssignmentSyncPacket
import com.cobblemon.mod.common.net.messages.client.data.SpeciesRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.data.StandardSpeciesFeatureSyncPacket
import com.cobblemon.mod.common.net.messages.client.data.UnlockReloadPacket
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueClosedPacket
import com.cobblemon.mod.common.net.messages.client.dialogue.DialogueOpenedPacket
import com.cobblemon.mod.common.net.messages.client.effect.RunPosableMoLangPacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormEntityParticlePacket
import com.cobblemon.mod.common.net.messages.client.effect.SpawnSnowstormParticlePacket
import com.cobblemon.mod.common.net.messages.client.fossil.FossilRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.fossil.NaturalMaterialRegistrySyncPacket
import com.cobblemon.mod.common.net.messages.client.pasture.ClosePasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.OpenPasturePacket
import com.cobblemon.mod.common.net.messages.client.pasture.PokemonPasturedPacket
import com.cobblemon.mod.common.net.messages.client.pasture.PokemonUnpasturedPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.*
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.AddEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.ClearEvolutionsPacket
import com.cobblemon.mod.common.net.messages.client.pokemon.update.evolution.RemoveEvolutionPacket
import com.cobblemon.mod.common.net.messages.client.settings.ServerSettingsPacket
import com.cobblemon.mod.common.net.messages.client.sound.UnvalidatedPlaySoundS2CPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnGenericBedrockPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokeballPacket
import com.cobblemon.mod.common.net.messages.client.spawn.SpawnPokemonPacket
import com.cobblemon.mod.common.net.messages.client.starter.OpenStarterUIPacket
import com.cobblemon.mod.common.net.messages.client.starter.SetClientPlayerDataPacket
import com.cobblemon.mod.common.net.messages.client.storage.RemoveClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.SwapClientPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.InitializePartyPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.MoveClientPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.party.SetPartyReferencePacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.InitializePCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.MoveClientPCPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.OpenPCPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCBoxPokemonPacket
import com.cobblemon.mod.common.net.messages.client.storage.pc.SetPCPokemonPacket
import com.cobblemon.mod.common.net.messages.client.toast.ToastPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeAcceptanceChangedPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeCancelledPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeCompletedPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeOfferExpiredPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeOfferNotificationPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeStartedPacket
import com.cobblemon.mod.common.net.messages.client.trade.TradeUpdatedPacket
import com.cobblemon.mod.common.net.messages.client.ui.InteractPokemonUIPacket
import com.cobblemon.mod.common.net.messages.client.ui.SummaryUIPacket
import com.cobblemon.mod.common.net.messages.server.*
import com.cobblemon.mod.common.net.messages.server.battle.BattleSelectActionsPacket
import com.cobblemon.mod.common.net.messages.server.battle.RemoveSpectatorPacket
import com.cobblemon.mod.common.net.messages.server.battle.SpectateBattlePacket
import com.cobblemon.mod.common.net.messages.server.callback.move.MoveSelectCancelledPacket
import com.cobblemon.mod.common.net.messages.server.callback.move.MoveSelectedPacket
import com.cobblemon.mod.common.net.messages.server.callback.party.PartyPokemonSelectedPacket
import com.cobblemon.mod.common.net.messages.server.callback.party.PartySelectCancelledPacket
import com.cobblemon.mod.common.net.messages.server.callback.partymove.PartyMoveSelectCancelledPacket
import com.cobblemon.mod.common.net.messages.server.callback.partymove.PartyPokemonMoveSelectedPacket
import com.cobblemon.mod.common.net.messages.server.dialogue.EscapeDialoguePacket
import com.cobblemon.mod.common.net.messages.server.dialogue.InputToDialoguePacket
import com.cobblemon.mod.common.net.messages.server.pasture.PasturePokemonPacket
import com.cobblemon.mod.common.net.messages.server.pasture.UnpastureAllPokemonPacket
import com.cobblemon.mod.common.net.messages.server.pasture.UnpasturePokemonPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.interact.InteractPokemonPacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.SetNicknamePacket
import com.cobblemon.mod.common.net.messages.server.pokemon.update.evolution.AcceptEvolutionPacket
import com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket
import com.cobblemon.mod.common.net.messages.server.storage.SwapPCPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.ReleasePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePCPokemonToPartyPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.MovePartyPokemonToPCPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.ReleasePCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.SwapPCPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.pc.UnlinkPlayerFromPCPacket
import com.cobblemon.mod.common.net.messages.server.trade.AcceptTradeRequestPacket
import com.cobblemon.mod.common.net.messages.server.trade.CancelTradePacket
import com.cobblemon.mod.common.net.messages.server.trade.ChangeTradeAcceptancePacket
import com.cobblemon.mod.common.net.messages.server.trade.OfferTradePacket
import com.cobblemon.mod.common.net.messages.server.trade.UpdateTradeOfferPacket
import com.cobblemon.mod.common.net.serverhandling.ChallengeHandler
import com.cobblemon.mod.common.net.serverhandling.RequestInteractionsHandler
import com.cobblemon.mod.common.net.serverhandling.battle.BattleSelectActionsHandler
import com.cobblemon.mod.common.net.serverhandling.battle.RemoveSpectatorHandler
import com.cobblemon.mod.common.net.serverhandling.battle.SpectateBattleHandler
import com.cobblemon.mod.common.net.serverhandling.callback.move.MoveSelectCancelledHandler
import com.cobblemon.mod.common.net.serverhandling.callback.move.MoveSelectedHandler
import com.cobblemon.mod.common.net.serverhandling.callback.party.PartyPokemonSelectedHandler
import com.cobblemon.mod.common.net.serverhandling.callback.party.PartySelectCancelledHandler
import com.cobblemon.mod.common.net.serverhandling.callback.partymove.PartyMoveSelectCancelledHandler
import com.cobblemon.mod.common.net.serverhandling.callback.partymove.PartyPokemonMoveSelectedHandler
import com.cobblemon.mod.common.net.serverhandling.dialogue.EscapeDialogueHandler
import com.cobblemon.mod.common.net.serverhandling.dialogue.InputToDialogueHandler
import com.cobblemon.mod.common.net.serverhandling.evolution.AcceptEvolutionHandler
import com.cobblemon.mod.common.net.serverhandling.pasture.PasturePokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pasture.UnpastureAllPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pasture.UnpasturePokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pokemon.interact.InteractPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.pokemon.update.SetNicknameHandler
import com.cobblemon.mod.common.net.serverhandling.starter.RequestStarterScreenHandler
import com.cobblemon.mod.common.net.serverhandling.starter.SelectStarterPacketHandler
import com.cobblemon.mod.common.net.serverhandling.storage.BenchMoveHandler
import com.cobblemon.mod.common.net.serverhandling.storage.RequestMoveSwapHandler
import com.cobblemon.mod.common.net.serverhandling.storage.SendOutPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.SwapPCPartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.MovePartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.ReleasePCPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.party.SwapPartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePCPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePCPokemonToPartyHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.MovePartyPokemonToPCHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.ReleasePartyPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.SwapPCPokemonHandler
import com.cobblemon.mod.common.net.serverhandling.storage.pc.UnlinkPlayerFromPCHandler
import com.cobblemon.mod.common.net.serverhandling.trade.AcceptTradeRequestHandler
import com.cobblemon.mod.common.net.serverhandling.trade.CancelTradeHandler
import com.cobblemon.mod.common.net.serverhandling.trade.ChangeTradeAcceptanceHandler
import com.cobblemon.mod.common.net.serverhandling.trade.OfferTradeHandler
import com.cobblemon.mod.common.net.serverhandling.trade.UpdateTradeOfferHandler
import com.cobblemon.mod.common.util.server
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import kotlin.reflect.KClass

/**
 * Registers Cobblemon network packets.
 *
 * This class also contains short functions for dispatching our packets to a player, all players, or to the entire server.
 *
 * @author Hiroku, Licious
 * @since November 27th, 2021
 */
object CobblemonNetwork : NetworkManager {

    fun ServerPlayerEntity.sendPacket(packet: NetworkPacket<*>) = sendPacketToPlayer(this, packet)
    fun sendToServer(packet: NetworkPacket<*>) = this.sendPacketToServer(packet)
    fun sendToAllPlayers(packet: NetworkPacket<*>) = sendPacketToPlayers(server()!!.playerManager.playerList, packet)
    fun sendPacketToPlayers(players: Iterable<ServerPlayerEntity>, packet: NetworkPacket<*>) = players.forEach { sendPacketToPlayer(it, packet) }

    override fun registerClientBound() {
        // Pokemon Update Packets
        this.createClientBound(FriendshipUpdatePacket.ID, FriendshipUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(MoveSetUpdatePacket.ID, MoveSetUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(NatureUpdatePacket.ID, NatureUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(ShinyUpdatePacket.ID, ShinyUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(SpeciesUpdatePacket.ID, SpeciesUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(NicknameUpdatePacket.ID, NicknameUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(HealthUpdatePacket.ID, HealthUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(ExperienceUpdatePacket.ID, ExperienceUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(StatusUpdatePacket.ID, StatusUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(CaughtBallUpdatePacket.ID, CaughtBallUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(BenchedMovesUpdatePacket.ID, BenchedMovesUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(GenderUpdatePacket.ID, GenderUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(AspectsUpdatePacket.ID, AspectsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(AbilityUpdatePacket.ID, AbilityUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(EVsUpdatePacket.ID, EVsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(IVsUpdatePacket.ID, IVsUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(HeldItemUpdatePacket.ID, HeldItemUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(PokemonStateUpdatePacket.ID, PokemonStateUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(TetheringUpdatePacket.ID, TetheringUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(TradeableUpdatePacket.ID, TradeableUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(SpeciesFeatureUpdatePacket.ID, SpeciesFeatureUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(OriginalTrainerUpdatePacket.ID, OriginalTrainerUpdatePacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(FormUpdatePacket.ID, FormUpdatePacket::decode, PokemonUpdatePacketHandler())

        // Evolution start
        this.createClientBound(AddEvolutionPacket.ID, AddEvolutionPacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(ClearEvolutionsPacket.ID, ClearEvolutionsPacket::decode, PokemonUpdatePacketHandler())
        this.createClientBound(RemoveEvolutionPacket.ID, RemoveEvolutionPacket::decode, PokemonUpdatePacketHandler())
        // Evolution End

        // Storage Packets
        this.createClientBound(InitializePartyPacket.ID, InitializePartyPacket::decode, InitializePartyHandler)
        this.createClientBound(SetPartyPokemonPacket.ID, SetPartyPokemonPacket::decode, SetPartyPokemonHandler)
        this.createClientBound(MoveClientPartyPokemonPacket.ID, MoveClientPartyPokemonPacket::decode, MoveClientPartyPokemonHandler)
        this.createClientBound(SetPartyReferencePacket.ID, SetPartyReferencePacket::decode, SetPartyReferenceHandler)

        this.createClientBound(InitializePCPacket.ID, InitializePCPacket::decode, InitializePCHandler)
        this.createClientBound(MoveClientPCPokemonPacket.ID, MoveClientPCPokemonPacket::decode, MoveClientPCPokemonHandler)
        this.createClientBound(SetPCBoxPokemonPacket.ID, SetPCBoxPokemonPacket::decode, SetPCBoxPokemonHandler)
        this.createClientBound(SetPCPokemonPacket.ID, SetPCPokemonPacket::decode, SetPCPokemonHandler)
        this.createClientBound(OpenPCPacket.ID, OpenPCPacket::decode, OpenPCHandler)
        this.createClientBound(ClosePCPacket.ID, ClosePCPacket::decode, ClosePCHandler)

        this.createClientBound(SwapClientPokemonPacket.ID, SwapClientPokemonPacket::decode, SwapClientPokemonHandler)
        this.createClientBound(RemoveClientPokemonPacket.ID, RemoveClientPokemonPacket::decode, RemoveClientPokemonHandler)

        // UI Packets
        this.createClientBound(SummaryUIPacket.ID, SummaryUIPacket::decode, SummaryUIPacketHandler)
        this.createClientBound(InteractPokemonUIPacket.ID, InteractPokemonUIPacket::decode, InteractPokemonUIPacketHandler)
        this.createClientBound(PlayerInteractOptionsPacket.ID, PlayerInteractOptionsPacket::decode, PlayerInteractOptionsHandler)

        // Starter packets
        this.createClientBound(OpenStarterUIPacket.ID, OpenStarterUIPacket::decode, StarterUIPacketHandler)
        this.createClientBound(SetClientPlayerDataPacket.ID, SetClientPlayerDataPacket::decode, SetClientPlayerDataHandler)

        // Battle packets
        this.createClientBound(BattleEndPacket.ID, BattleEndPacket::decode, BattleEndHandler)
        this.createClientBound(BattleInitializePacket.ID, BattleInitializePacket::decode, BattleInitializeHandler)
        this.createClientBound(BattleQueueRequestPacket.ID, BattleQueueRequestPacket::decode, BattleQueueRequestHandler)
        this.createClientBound(BattleFaintPacket.ID, BattleFaintPacket::decode, BattleFaintHandler)
        this.createClientBound(BattleMakeChoicePacket.ID, BattleMakeChoicePacket::decode, BattleMakeChoiceHandler)
        this.createClientBound(BattleHealthChangePacket.ID, BattleHealthChangePacket::decode, BattleHealthChangeHandler)
        this.createClientBound(BattleSetTeamPokemonPacket.ID, BattleSetTeamPokemonPacket::decode, BattleSetTeamPokemonHandler)
        this.createClientBound(BattleSwitchPokemonPacket.ID, BattleSwitchPokemonPacket::decode, BattleSwitchPokemonHandler)
        this.createClientBound(BattleMessagePacket.ID, BattleMessagePacket::decode, BattleMessageHandler)
        this.createClientBound(BattleCaptureStartPacket.ID, BattleCaptureStartPacket::decode, BattleCaptureStartHandler)
        this.createClientBound(BattleCaptureEndPacket.ID, BattleCaptureEndPacket::decode, BattleCaptureEndHandler)
        this.createClientBound(BattleCaptureShakePacket.ID, BattleCaptureShakePacket::decode, BattleCaptureShakeHandler)
        this.createClientBound(BattleApplyPassResponsePacket.ID, BattleApplyPassResponsePacket::decode, BattleApplyPassResponseHandler)
        this.createClientBound(BattleChallengeNotificationPacket.ID, BattleChallengeNotificationPacket::decode, BattleChallengeNotificationHandler)
        this.createClientBound(BattleUpdateTeamPokemonPacket.ID, BattleUpdateTeamPokemonPacket::decode, BattleUpdateTeamPokemonHandler)
        this.createClientBound(BattlePersistentStatusPacket.ID, BattlePersistentStatusPacket::decode, BattlePersistentStatusHandler)
        this.createClientBound(BattleMadeInvalidChoicePacket.ID, BattleMadeInvalidChoicePacket::decode, BattleMadeInvalidChoiceHandler)
        this.createClientBound(BattleMusicPacket.ID, BattleMusicPacket::decode, BattleMusicHandler)
        this.createClientBound(BattleChallengeExpiredPacket.ID, BattleChallengeExpiredPacket::decode, BattleChallengeExpiredHandler)
        this.createClientBound(BattleReplacePokemonPacket.ID, BattleReplacePokemonPacket::decode, BattleReplacePokemonHandler)
        this.createClientBound(BattleTransformPokemonPacket.ID, BattleTransformPokemonPacket::decode, BattleTransformPokemonHandler)



        // Settings packets
        this.createClientBound(ServerSettingsPacket.ID, ServerSettingsPacket::decode, ServerSettingsPacketHandler)

        // Data registries
        this.createClientBound(AbilityRegistrySyncPacket.ID, AbilityRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(MovesRegistrySyncPacket.ID, MovesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(SpeciesRegistrySyncPacket.ID, SpeciesRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(PropertiesCompletionRegistrySyncPacket.ID, PropertiesCompletionRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(UnlockReloadPacket.ID, UnlockReloadPacket::decode, UnlockReloadPacketHandler)
        this.createClientBound(BerryRegistrySyncPacket.ID, BerryRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(StandardSpeciesFeatureSyncPacket.ID, StandardSpeciesFeatureSyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(GlobalSpeciesFeatureSyncPacket.ID, GlobalSpeciesFeatureSyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(SpeciesFeatureAssignmentSyncPacket.ID, SpeciesFeatureAssignmentSyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(NaturalMaterialRegistrySyncPacket.ID, NaturalMaterialRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())
        this.createClientBound(FossilRegistrySyncPacket.ID, FossilRegistrySyncPacket::decode, DataRegistrySyncPacketHandler())

        // Effects
        this.createClientBound(SpawnSnowstormParticlePacket.ID, SpawnSnowstormParticlePacket::decode, SpawnSnowstormParticleHandler)
        this.createClientBound(SpawnSnowstormEntityParticlePacket.ID, SpawnSnowstormEntityParticlePacket::decode, SpawnSnowstormEntityParticleHandler)
        this.createClientBound(RunPosableMoLangPacket.ID, RunPosableMoLangPacket::decode, RunPosableMoLangHandler)

        // Hax
        this.createClientBound(UnvalidatedPlaySoundS2CPacket.ID, UnvalidatedPlaySoundS2CPacket::decode, UnvalidatedPlaySoundS2CPacketHandler)
        this.createClientBound(SpawnPokemonPacket.ID, SpawnPokemonPacket::decode, SpawnExtraDataEntityHandler())
        this.createClientBound(SpawnPokeballPacket.ID, SpawnPokeballPacket::decode, SpawnExtraDataEntityHandler())
        this.createClientBound(ToastPacket.ID, ToastPacket::decode, ToastPacketHandler)
        this.createClientBound(SpawnGenericBedrockPacket.ID, SpawnGenericBedrockPacket::decode, SpawnExtraDataEntityHandler())

        // Trade packets
        this.createClientBound(TradeAcceptanceChangedPacket.ID, TradeAcceptanceChangedPacket::decode, TradeAcceptanceChangedHandler)
        this.createClientBound(TradeCancelledPacket.ID, TradeCancelledPacket::decode, TradeCancelledHandler)
        this.createClientBound(TradeCompletedPacket.ID, TradeCompletedPacket::decode, TradeCompletedHandler)
        this.createClientBound(TradeUpdatedPacket.ID, TradeUpdatedPacket::decode, TradeUpdatedHandler)
        this.createClientBound(TradeOfferNotificationPacket.ID, TradeOfferNotificationPacket::decode, TradeOfferNotificationHandler)
        this.createClientBound(TradeOfferExpiredPacket.ID, TradeOfferExpiredPacket::decode, TradeOfferExpiredHandler)
        this.createClientBound(TradeStartedPacket.ID, TradeStartedPacket::decode, TradeStartedHandler)

        // Pasture
        this.createClientBound(OpenPasturePacket.ID, OpenPasturePacket::decode, OpenPastureHandler)
        this.createClientBound(ClosePasturePacket.ID, ClosePasturePacket::decode, ClosePastureHandler)
        this.createClientBound(PokemonPasturedPacket.ID, PokemonPasturedPacket::decode, PokemonPasturedHandler)
        this.createClientBound(PokemonUnpasturedPacket.ID, PokemonUnpasturedPacket::decode, PokemonUnpasturedHandler)

        // Behaviours
        this.createClientBound(PlayPosableAnimationPacket.ID, PlayPosableAnimationPacket::decode, PlayPosableAnimationHandler)

        // Move select packets
        this.createClientBound(OpenMoveCallbackPacket.ID, OpenMoveCallbackPacket::decode, OpenMoveCallbackHandler)

        // Party select packets
        this.createClientBound(OpenPartyCallbackPacket.ID, OpenPartyCallbackPacket::decode, OpenPartyCallbackHandler)

        // Party move select packets
        this.createClientBound(OpenPartyMoveCallbackPacket.ID, OpenPartyMoveCallbackPacket::decode, OpenPartyMoveCallbackHandler)

        // Dialogue packets
        this.createClientBound(DialogueClosedPacket.ID, DialogueClosedPacket::decode, DialogueClosedHandler)
        this.createClientBound(DialogueOpenedPacket.ID, DialogueOpenedPacket::decode, DialogueOpenedHandler)
    }

    override fun registerServerBound() {
        // Pokemon Update Packets
        this.createServerBound(SetNicknamePacket.ID, SetNicknamePacket::decode, SetNicknameHandler)

        // Evolution Packets
        this.createServerBound(AcceptEvolutionPacket.ID, AcceptEvolutionPacket::decode, AcceptEvolutionHandler)

        // Interaction Packets
        this.createServerBound(InteractPokemonPacket.ID, InteractPokemonPacket::decode, InteractPokemonHandler)
        this.createServerBound(RequestPlayerInteractionsPacket.ID, RequestPlayerInteractionsPacket::decode, RequestInteractionsHandler)

        // Storage Packets
        this.createServerBound(SendOutPokemonPacket.ID, SendOutPokemonPacket::decode, SendOutPokemonHandler)
        this.createServerBound(RequestMoveSwapPacket.ID, RequestMoveSwapPacket::decode, RequestMoveSwapHandler)
        this.createServerBound(BenchMovePacket.ID, BenchMovePacket::decode, BenchMoveHandler)
        this.createServerBound(BattleChallengePacket.ID, BattleChallengePacket::decode, ChallengeHandler)

        this.createServerBound(MovePCPokemonToPartyPacket.ID, MovePCPokemonToPartyPacket::decode, MovePCPokemonToPartyHandler)
        this.createServerBound(MovePartyPokemonToPCPacket.ID, MovePartyPokemonToPCPacket::decode, MovePartyPokemonToPCHandler)
        this.createServerBound(ReleasePartyPokemonPacket.ID, ReleasePartyPokemonPacket::decode, ReleasePartyPokemonHandler)
        this.createServerBound(ReleasePCPokemonPacket.ID, ReleasePCPokemonPacket::decode, ReleasePCPokemonHandler)
        this.createServerBound(UnlinkPlayerFromPCPacket.ID, UnlinkPlayerFromPCPacket::decode, UnlinkPlayerFromPCHandler)

        // Starter packets
        this.createServerBound(SelectStarterPacket.ID, SelectStarterPacket::decode, SelectStarterPacketHandler)
        this.createServerBound(RequestStarterScreenPacket.ID, RequestStarterScreenPacket::decode, RequestStarterScreenHandler)

        this.createServerBound(SwapPCPokemonPacket.ID, SwapPCPokemonPacket::decode, SwapPCPokemonHandler)
        this.createServerBound(SwapPartyPokemonPacket.ID, SwapPartyPokemonPacket::decode, SwapPartyPokemonHandler)

        this.createServerBound(MovePCPokemonPacket.ID, MovePCPokemonPacket::decode, MovePCPokemonHandler)
        this.createServerBound(MovePartyPokemonPacket.ID, MovePartyPokemonPacket::decode, MovePartyPokemonHandler)

        this.createServerBound(SwapPCPartyPokemonPacket.ID, SwapPCPartyPokemonPacket::decode, SwapPCPartyPokemonHandler)

        // Battle packets
        this.createServerBound(BattleSelectActionsPacket.ID, BattleSelectActionsPacket::decode, BattleSelectActionsHandler)
        this.createServerBound(SpectateBattlePacket.ID, SpectateBattlePacket::decode, SpectateBattleHandler)
        this.createServerBound(RemoveSpectatorPacket.ID, RemoveSpectatorPacket::decode, RemoveSpectatorHandler)

        // Trade
        this.createServerBound(AcceptTradeRequestPacket.ID, AcceptTradeRequestPacket::decode, AcceptTradeRequestHandler)
        this.createServerBound(CancelTradePacket.ID, CancelTradePacket::decode, CancelTradeHandler)
        this.createServerBound(ChangeTradeAcceptancePacket.ID, ChangeTradeAcceptancePacket::decode, ChangeTradeAcceptanceHandler)
        this.createServerBound(OfferTradePacket.ID, OfferTradePacket::decode, OfferTradeHandler)
        this.createServerBound(UpdateTradeOfferPacket.ID, UpdateTradeOfferPacket::decode, UpdateTradeOfferHandler)

        // Pasture
        this.createServerBound(PasturePokemonPacket.ID, PasturePokemonPacket::decode, PasturePokemonHandler)
        this.createServerBound(UnpasturePokemonPacket.ID, UnpasturePokemonPacket::decode, UnpasturePokemonHandler)
        this.createServerBound(UnpastureAllPokemonPacket.ID, UnpastureAllPokemonPacket::decode, UnpastureAllPokemonHandler)

        // Move select packets
        this.createServerBound(MoveSelectedPacket.ID, MoveSelectedPacket::decode, MoveSelectedHandler)
        this.createServerBound(MoveSelectCancelledPacket.ID, MoveSelectCancelledPacket::decode, MoveSelectCancelledHandler)

        // Party select packets
        this.createServerBound(PartyPokemonSelectedPacket.ID, PartyPokemonSelectedPacket::decode, PartyPokemonSelectedHandler)
        this.createServerBound(PartySelectCancelledPacket.ID, PartySelectCancelledPacket::decode, PartySelectCancelledHandler)

        // Party move select packets
        this.createServerBound(PartyPokemonMoveSelectedPacket.ID, PartyPokemonMoveSelectedPacket::decode, PartyPokemonMoveSelectedHandler)
        this.createServerBound(PartyMoveSelectCancelledPacket.ID, PartyMoveSelectCancelledPacket::decode, PartyMoveSelectCancelledHandler)

        // Dialogue packets
        this.createServerBound(EscapeDialoguePacket.ID, EscapeDialoguePacket::decode, EscapeDialogueHandler)
        this.createServerBound(InputToDialoguePacket.ID, InputToDialoguePacket::decode, InputToDialogueHandler)
    }

    private inline fun <reified T : NetworkPacket<T>> createClientBound(identifier: Identifier, noinline decoder: (PacketByteBuf) -> T, handler: ClientNetworkPacketHandler<T>) {
        Cobblemon.implementation.networkManager.createClientBound(identifier, T::class, { message, buffer -> message.encode(buffer) }, decoder, handler)
    }

    private inline fun <reified T : NetworkPacket<T>> createServerBound(identifier: Identifier, noinline decoder: (PacketByteBuf) -> T, handler: ServerNetworkPacketHandler<T>) {
        Cobblemon.implementation.networkManager.createServerBound(identifier, T::class, { message, buffer -> message.encode(buffer) }, decoder, handler)
    }

    override fun <T : NetworkPacket<T>> createClientBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ClientNetworkPacketHandler<T>
    ) {
        Cobblemon.implementation.networkManager.createClientBound(identifier, kClass, encoder, decoder, handler)
    }

    override fun <T : NetworkPacket<T>> createServerBound(
        identifier: Identifier,
        kClass: KClass<T>,
        encoder: (T, PacketByteBuf) -> Unit,
        decoder: (PacketByteBuf) -> T,
        handler: ServerNetworkPacketHandler<T>
    ) {
        Cobblemon.implementation.networkManager.createServerBound(identifier, kClass, encoder, decoder, handler)
    }

    override fun sendPacketToPlayer(player: ServerPlayerEntity, packet: NetworkPacket<*>) = Cobblemon.implementation.networkManager.sendPacketToPlayer(player, packet)

    override fun sendPacketToServer(packet: NetworkPacket<*>) = Cobblemon.implementation.networkManager.sendPacketToServer(packet)

    override fun <T : NetworkPacket<*>> asVanillaClientBound(packet: T): Packet<ClientPlayPacketListener> = Cobblemon.implementation.networkManager.asVanillaClientBound(packet)
}