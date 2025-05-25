
package org.blog.uuid;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public final class GetUuidPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("GET_UUID_PLUGIN 가동 시작 ");
    }

    @Override
    public void onDisable() {
        getLogger().info("GET_UUID_PLUGIN 가동 종료 ");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("get_uuid")) {
            if (args.length != 1) {
                sender.sendMessage("사용법: /get_uuid <닉네임>");
                return true;
            }

            String nickname = args[0];
            sender.sendMessage("UUID를 조회 중입니다...");

            // 비동기로 실행 (서버 렉 방지)
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    String apiUrl = "https://api.mojang.com/users/profiles/minecraft/" + nickname;
                    HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                        in.close();

                        JSONParser parser = new JSONParser();
                        JSONObject json = (JSONObject) parser.parse(response.toString());
                        String uuid = (String) json.get("id");
                        String formattedUUID = uuid.replaceFirst(
                                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                                "$1-$2-$3-$4-$5"
                        );

                        sender.sendMessage("닉네임: " + nickname);
                        sender.sendMessage("UUID: " + formattedUUID);
                    } else if (responseCode == 204) {
                        sender.sendMessage("존재하지 않는 닉네임입니다.");
                    } else {
                        sender.sendMessage("UUID 조회 실패: HTTP 오류 코드 " + responseCode);
                    }
                } catch (Exception e) {
                    sender.sendMessage("오류 발생: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            return true;
        }

        return false;
    }
}
