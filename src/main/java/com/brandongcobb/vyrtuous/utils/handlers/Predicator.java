/*  Predicator.java The purpose of this program is to run lambda functions
 *  before executing code on the main thread.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.vyrtuous.utils.handlers;

import com.brandongcobb.vyrtuous.Vyrtuous;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.CompletableFuture;

public class Predicator {

    private Vyrtuous app;
    private JDA bot;

    public Predicator(JDA bot) {
        this.bot = bot;
    }

    public CompletableFuture<Guild> getGuildById(long guildId) {
        return CompletableFuture.supplyAsync(() -> {
            for (Guild guild : bot.getGuilds()) {
                if (Long.parseLong(guild.getId()) == guildId) {
                    return guild;
                }
            }
            return null;
        });
    }
}
