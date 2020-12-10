package com.luanegra.rsachat.modelclasses


class Chat {
    private var sender: String = ""
    private var reciever: String = ""
    private var message: String = ""
    private var timeStamp: String = ""
    private var isseen: Boolean = false
    private var messageId: String = ""
    private var url: String = ""



    constructor(
        sender: String,
        reciever: String,
        message: String,
        timeStamp: String,
        isseen: Boolean,
        messageId: String,
        url: String
    ) {
        this.sender = sender
        this.reciever = reciever
        this.message = message
        this.timeStamp = timeStamp
        this.isseen = isseen
        this.messageId = messageId
        this.url = url

    }

    constructor()

    fun getMessageid(): String{
        return messageId
    }

    fun setMessageid(messageId: String){
        this.messageId = messageId
    }
    fun getsender(): String{
        return sender
    }

    fun setsender(sender: String){
        this.sender = sender
    }
    fun getreciever(): String{
        return reciever
    }

    fun setreciever(reciever: String){
        this.reciever = reciever
    }
    fun getmessage(): String{
        return message
    }

    fun setmessage(message: String){
        this.message = message
    }
    fun gettimeStamp(): String{
        return timeStamp
    }

    fun settimeStamp(timeStamp: String){
        this.timeStamp = timeStamp
    }
    fun getisseen(): Boolean{
        return isseen
    }

    fun setisseen(isseen: Boolean){
        this.isseen = isseen
    }
    fun geturl(): String{
        return url
    }

    fun seturl(url: String){
        this.url = url
    }

}