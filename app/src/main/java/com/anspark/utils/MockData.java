package com.anspark.utils;

import com.anspark.models.AuthResponse;
import com.anspark.models.Chat;
import com.anspark.models.Match;
import com.anspark.models.Message;
import com.anspark.models.Photo;
import com.anspark.models.Profile;
import com.anspark.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class MockData {
    private MockData() {
    }

    private static String id(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString();
    }

    public static AuthResponse sampleAuthResponse() {
        User user = new User("user_1", "Adrian", "adrian@anspark.com");
        return new AuthResponse("mock_access_token", "mock_refresh_token", user);
    }

    public static Profile sampleProfile() {
        Profile profile = new Profile();
        profile.setId("profile_me");
        profile.setName("Adrian");
        profile.setAge(27);
        profile.setCity("Warszawa");
        profile.setBio("Frontend, silownia, fotografia analogowa.");
        profile.setTags(Arrays.asList("Tech", "Silownia", "Fotografia"));

        List<Photo> photos = new ArrayList<>();
        photos.add(new Photo(id("photo"), "local://male_profile", true));
        photos.add(new Photo(id("photo"), "local://male_profile", false));
        profile.setPhotos(photos);
        return profile;
    }

    public static List<Profile> sampleDiscoverProfiles() {
        List<Profile> profiles = new ArrayList<>();

        Profile maja = new Profile();
        maja.setId("profile_maja");
        maja.setName("Maja");
        maja.setAge(24);
        maja.setBio("Uwielbiam gory, analogowe zdjecia i nocne spacery po miescie.");
        maja.setCity("Krakow");
        maja.setTags(Arrays.asList("Gory", "Kino", "Podroze"));
        maja.setPhotos(Arrays.asList(new Photo(id("photo"), "local://female_profile_1", true)));
        profiles.add(maja);

        Profile kasia = new Profile();
        kasia.setId("profile_kasia");
        kasia.setName("Kasia");
        kasia.setAge(26);
        kasia.setBio("Biegam o poranku, lubie kino i szukam kogos z dobra energia.");
        kasia.setCity("Poznan");
        kasia.setTags(Arrays.asList("Bieganie", "Kino", "Kawa"));
        kasia.setPhotos(Arrays.asList(new Photo(id("photo"), "local://female_profile_2", true)));
        profiles.add(kasia);

        return profiles;
    }

    public static List<Match> sampleMatches() {
        List<Match> matches = new ArrayList<>();
        for (Profile profile : sampleDiscoverProfiles()) {
            matches.add(new Match("match_" + profile.getId(), profile, true, "now"));
        }
        return matches;
    }

    public static List<Chat> sampleChats() {
        List<Chat> chats = new ArrayList<>();

        Profile maja = new Profile();
        maja.setId("profile_maja");
        maja.setName("Maja");
        maja.setAge(24);
        maja.setPhotos(Arrays.asList(new Photo(id("photo"), "local://female_profile_1", true)));

        Message lastMaja = new Message(id("msg"), "chat_maja", "profile_maja", "Jutro po 18:00 mam wolne, pasuje Ci?", "09:12", false);
        Chat chatMaja = new Chat("chat_maja", maja, lastMaja, "09:12");
        chats.add(chatMaja);

        Profile kasia = new Profile();
        kasia.setId("profile_kasia");
        kasia.setName("Kasia");
        kasia.setAge(26);
        kasia.setPhotos(Arrays.asList(new Photo(id("photo"), "local://female_profile_2", true)));

        Message lastKasia = new Message(id("msg"), "chat_kasia", "profile_kasia", "Dzieki za super rozmowe wczoraj.", "Wczoraj", false);
        Chat chatKasia = new Chat("chat_kasia", kasia, lastKasia, "Wczoraj");
        chats.add(chatKasia);

        return chats;
    }

    public static List<Message> sampleMessages(String chatId) {
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(id("msg"), chatId, "profile_maja", "Czesc! Widzialam, ze tez lubisz trekking.", "09:10", false));
        messages.add(new Message(id("msg"), chatId, "user_1", "Tak, najczesciej wypady w gory na weekend.", "09:11", true));
        messages.add(new Message(id("msg"), chatId, "profile_maja", "Super! To moze kawa i plan na mini wyjazd?", "09:12", false));
        return messages;
    }
}
