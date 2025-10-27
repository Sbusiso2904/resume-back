import express from "express";


function Password(){
    const passwordRules = {
        minlength: 8,
        maxlength: 16,
        charuppercase: true,
        charlowercase: true,
        requireNumber: true,
        requireSpecialChar: true,
        noCommonPasswords: true,

    };

    // Sign_in protocal
    const emails = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    //username validation
    const usernameRules = {
        minlength: 6,
        maxlength: 10,
        allowedChars: /^[a-zA-Z0-9_-]+$/,
    };

}