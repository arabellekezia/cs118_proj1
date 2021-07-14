var express = require('express');
var url = require('url');
var db = require("../db")
var config = require('../config')
var bcrypt = require('bcrypt');
var commonmark = require('commonmark');
var jwt = require('jsonwebtoken');
var cookieParser = require('cookie-parser');

var router = express.Router();
var parser = new commonmark.Parser();
var renderer = new commonmark.HtmlRenderer();

// router.use(cookieParser());

// GET method for login
router.get('/', function(req, res, next){
  let username = req.body.username;
  let pswd = req.body.password;
  let redirect = req.body.redirect;

  // if no username and pswd return 401 error
  if (username === undefined || pswd === NaN){
    res.render('login', {redirect:redirect});
  } else{
    db.getPassword(username, function(user){
      if (user == null){
        res.status(401);
      }
      else{
        bcrypt.compare(pswd, user.password, function(err, valid){
          if (!valid){
            res.status(401);
            res.render('login', {redirect:redirect});
          } else{
            let payload = { usr:username };
            let header = {
              "alg": "HS256",
              "typ": "JWT"
            };
            let options = {
              algorithm:"HS256",
              expiresIn:"2h",
              header:header
            };
            jwt.sign(payload, config.secret_key, options, function(err, token){
              if (token != null){
                res.cookie('jwt', token);
                if (!redirect){
                  res.status(200);
                  res.render('error', {code:"200", message:"Authentication successful"});
                } else{
                  res.status(302).redirect(redirect);
                }
              } else{
                res.sendStatus(401);
              }
            });
          }
        });
      }
    });
  }
});

// PUT method for login
router.post('/', function(req, res, next){
  let username = req.body.username;
  let pswd = req.body.password;
  let redirect = req.body.redirect;

  // if no username and pswd return 401 error
  if (!username || !pswd){
    res.status(401);
    res.render('error', {code:"401", message:"Unauthorized request"});
  }
  db.getPassword(username, function(user){
    if (user == null){
      res.status(404);
      res.render('error', {code:"404", message:"User and password combination not found"});
    }
    else{
      bcrypt.compare(pswd, user.password, function(err, valid){
        if (!valid){
          res.status(401);
          res.render('login', {redirect:redirect});
        } else{
          let payload = { usr:username };
          let header = {
            "alg": "HS256",
            "typ": "JWT"
          };
          let options = {
            algorithm:"HS256",
            expiresIn:"2h",
            header:header
          };
          jwt.sign(payload, config.secret_key, options, function(err, token){
            if (token != null){
              res.cookie('jwt', token);
              if (!redirect){
                res.status(200);
                res.render('error', {code:"200", message:"Authentication successful"});
              } else{
                res.status(302).redirect(redirect);
              }
            } else{
              res.sendStatus(401);
            }
          });
        }
      });
    }
  });
});

module.exports = router;
