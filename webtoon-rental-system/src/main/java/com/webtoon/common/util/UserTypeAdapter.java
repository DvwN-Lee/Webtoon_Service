package com.webtoon.common.util;

import com.google.gson.*;
import com.webtoon.domain.User;
import com.webtoon.domain.Author;
import com.webtoon.domain.Reader;
import java.lang.reflect.Type;

/**
 * User 추상 클래스를 위한 Gson TypeAdapter
 * NFR-STOR-01: JSON 파일 저장 요구사항 준수
 */
public class UserTypeAdapter implements JsonDeserializer<User>, JsonSerializer<User> {

    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // nickname 필드가 있으면 Reader, authorName이 있으면 Author
        if (jsonObject.has("nickname")) {
            return context.deserialize(json, Reader.class);
        } else if (jsonObject.has("authorName")) {
            return context.deserialize(json, Author.class);
        }

        throw new JsonParseException("Unknown User type: JSON must have 'nickname' or 'authorName' field");
    }

    @Override
    public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
        // 실제 타입(Reader/Author)으로 직렬화
        return context.serialize(src, src.getClass());
    }
}