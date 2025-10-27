import express from "express";


function Password(){
    const passwordRules = {
        minlength: 8,
        charuppercase: true,
        charlowercase: true,
        requireNumber: true,
        requireSpecialChar: true,
        noCommonPasswords: true,

    };

    // Sign_in protocal

    const emails = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

}