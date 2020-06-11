package br.svcdev.common.commands;

import java.io.Serializable;

public class UpdateNicknameCommand implements Serializable {
    private final String nickname;
    private final String newNickname;

    public UpdateNicknameCommand(String nickname, String newNickname) {
        this.nickname = nickname;
        this.newNickname = newNickname;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNewNickname() {
        return newNickname;
    }
}
