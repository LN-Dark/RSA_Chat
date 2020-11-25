package com.luanegra.rsachat.modelclasses

class ChatList {
    private var id: String = ""

    constructor()

    constructor(id: String) {
        this.id = id
    }

    fun getUid(): String?{
        return id
    }

    fun setUid(uid: String){
        this.id = id
    }


}