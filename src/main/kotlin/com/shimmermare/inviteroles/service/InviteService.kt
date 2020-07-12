package com.shimmermare.inviteroles.service

import com.shimmermare.inviteroles.asNullable
import com.shimmermare.inviteroles.entity.TrackedInvite
import com.shimmermare.inviteroles.repository.TrackedInviteRepository
import net.dv8tion.jda.api.entities.Guild
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InviteService(
    private val repository: TrackedInviteRepository
) {
    @Transactional(readOnly = true)
    fun getInvite(inviteCode: String): TrackedInvite? {
        return repository.findById(inviteCode).asNullable()
    }

    @Transactional
    fun getOrCreateInvite(inviteCode: String, guildId: Long): TrackedInvite {
        var invite = getInvite(inviteCode)
        if (invite == null) {
            invite = TrackedInvite(inviteCode, guildId)
            repository.save(invite)
        }
        return invite
    }

    @Transactional
    fun getOrCreateInvite(inviteCode: String, guild: Guild) = getOrCreateInvite(inviteCode, guild.idLong)

    @Transactional(readOnly = true)
    fun getInvitesOfGuild(guildId: Long) = repository.getAllOfGuild(guildId)

    @Transactional(readOnly = true)
    fun getInvitesOfGuild(guild: Guild) = getInvitesOfGuild(guild.idLong)

    @Transactional
    fun modifyInvite(code: String, block: (TrackedInvite?) -> TrackedInvite?): TrackedInvite? {
        val invite = getInvite(code)
        val newInvite = block.invoke(invite)
        if (invite == null) {
            if (newInvite == null) {
                // Do nothing
            } else {
                repository.save(newInvite)
            }
        } else {
            if (newInvite == null) {
                repository.delete(invite)
            } else {
                assertChangeLegal(invite, newInvite)
                repository.save(newInvite)
            }
        }
        return newInvite
    }

    private fun assertChangeLegal(before: TrackedInvite, after: TrackedInvite) {
        if (before.inviteCode != after.inviteCode) {
            throw IllegalStateException("Invite code change is not allowed when modifying invite")
        } else if (before.guildId != after.guildId) {
            throw IllegalStateException("Invite guild id change is not allowed when modifying invite")
        }
    }
}