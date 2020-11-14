package com.luanegra.rsachat.modelclasses

class Users {
    private var uid: String = ""
    private var username: String = ""
    private var email: String = ""
    private var profile: String = ""
    private var cover: String = ""
    private var status: String = ""
    private var search: String = ""
    private var facebook: String = ""
    private var instagram: String = ""
    private var website: String = ""



    constructor(
        uid: String,
        username: String,
        email: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        facebook: String,
        instagram: String,
        website: String
    ) {
        this.uid = uid
        this.username = username
        this.email = email
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.facebook = facebook
        this.instagram = instagram
        this.website = website
    }

    constructor()

    fun getUid(): String?{
        return uid
    }

    fun setUid(uid: String){
        this.uid = uid
    }

    fun getusername(): String?{
        return username
    }

    fun setusername(username: String){
        this.username = username
    }
    fun getemail(): String?{
        return email
    }

    fun setemail(email: String){
        this.email = email
    }
    fun getprofile(): String?{
        return profile
    }

    fun setprofile(profile: String){
        this.profile = profile
    }
    fun getcover(): String?{
        return cover
    }

    fun setcover(cover: String){
        this.cover = cover
    }
    fun getstatus(): String?{
        return status
    }

    fun setstatus(status: String){
        this.status = status
    }
    fun getsearch(): String?{
        return search
    }

    fun setsearch(search: String){
        this.search = search
    }

    fun getfacebook(): String?{
        return facebook
    }

    fun setfacebook(facebook: String){
        this.facebook = facebook
    }
    fun getinstagram(): String?{
        return instagram
    }

    fun setinstagram(instagram: String){
        this.instagram = instagram
    }
    fun getwebsite(): String?{
        return website
    }

    fun setwebsite(website: String){
        this.website = website
    }

}