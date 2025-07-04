/*  DiscordBot.java The purpose of this class is to manage the
 *  JDA discord api.
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
package com.brandongcobb.vyrtuous.component.bot;

import com.brandongcobb.vyrtuous.cogs.Cog;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class DiscordBot {

    private JDA api;
    private DiscordBot bot;
    private final Logger logger = Logger.getLogger("Vyrtuous");;
    private final ReentrantLock lock = new ReentrantLock();

    @Autowired
    public DiscordBot(ApplicationContext context) {
        this.bot = this;
        String apiKey = System.getenv("DISCORD_API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Discord API Key is null or empty");
        }
        try {
            this.api = JDABuilder.createDefault(apiKey,
                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MEMBERS)
                    .setActivity(Activity.playing("I take pharmacology personally."))
                    .build();
            Map<String, Cog> cogs = context.getBeansOfType(Cog.class);
            for (Cog cog : cogs.values()) {
                cog.register(api, this);
            }
            logger.info("Discord bot successfully initialized.");
            this.api.awaitReady();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during DiscordBot setup", e);
        }
    }

    public DiscordBot completeGetBot() {
        return this.bot;
    }

    @Bean
    public JDA getJDA() {
        return this.api;
    }

}
