package com.luanegra.rsachat.modelclasses

class Blocked {
    private var userID: String = ""
    private var conditionBlock: String = ""
    private var uid: String = ""

    constructor()

    constructor(userID: String, conditionBlock: String, uid: String) {
        this.userID = userID
        this.conditionBlock = conditionBlock
        this.uid = uid
    }

    fun getuserID(): String?{
        return userID
    }

    fun setuserID(userID: String){
        this.userID = userID
    }

    fun getconditionBlock(): String?{
        return conditionBlock
    }

    fun setconditionBlock(conditionBlock: String){
        this.conditionBlock = conditionBlock
    }

    fun getuid(): String?{
        return uid
    }

    fun setuid(uid: String){
        this.uid = uid
    }


}