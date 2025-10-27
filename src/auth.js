import express from "express";


function AuthPassandSign(){
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

function validatePassword(password) {
  const errors = [];

  if(!password.length < 8){
    errors.push("Password must be at least 8 characters!");
  }

  if(!/[A-Z]/.test(Password)){
    errors.push("Password must contain uppercase letters!")
  }

  if(!/[a-z]/.test(password)){
    errors.push("Password must contain lowercase letters!")
  }
  if(!/\d/.test(password)){
      errors.push("Password must contain numbers!")
  }

  if(!/[!@#$%^&*()-=+_/]/.test(password)){
      errors.push("Password must contain special characters!")
  }

}

export default AuthPassandSign;