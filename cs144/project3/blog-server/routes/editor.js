var express = require('express');
var config = require("../config")
var jwt = require('jsonwebtoken');
var router = express.Router();

var bodyParser = require('body-parser');
var cookieParser = require('cookie-parser');

// authentication method
router.all('*', function(req, res, next){
  var token = req.cookies.jwt;
  if (token){
    jwt.verify(token, config.secret_key, function(err, payload){
      if (err){
        res.sendStatus(302).redirect('/login?redirect=/editor/');
      } else{
        next();
      }
    });
  }
  else{
    res.sendStatus(302).redirect('/login?redirect=/editor/');
  }
});

module.exports = router;
