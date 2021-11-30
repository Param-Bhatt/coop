package com.example.coop

import com.google.firebase.Timestamp

class Post {
    var author : String ?= null
    var time : Timestamp?= null
    var title : String ?= null
    var body : String ?=  null
    var upvotes : Long ?= null
    var downvotes : Long ?= null
}