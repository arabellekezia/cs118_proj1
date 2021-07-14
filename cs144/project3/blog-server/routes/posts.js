var express = require('express');
var url = require('url');
var db = require("../db")
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
        res.sendStatus(401);
      } else{
        var usr = req.params.username || req.query.username || req.body.username;
        if (usr == payload.usr){
          next();
        } else{
          res.sendStatus(401);
        }
      }
    });
  } else{
    res.sendStatus(401);
  }
});

// GET: get user's blogs return json
router.get('/posts', function(req, res, next){
  let username = req.query.username;
  let postid = parseInt(req.query.postid);
  if (username === undefined){
    res.sendStatus(400);
  } else{
    if (!postid){
      db.getPosts2(username, function(posts){
        if (!posts){
          posts = {};
          res.status(200).send(posts);
        } else{
          res.status(200).send(posts);
        }
      });
    } else{
      db.getPost(username, postid, function(post){
        if (!post){
          res.sendStatus(404);
        } else{
          res.status(200).send(post);
        }
      });
    }
  }
});

router.delete('/posts', function(req, res, next){
  let username = req.query.username;
  let postid = parseInt(req.query.postid);
  if (username === undefined || postid === NaN){
    res.sendStatus(400);
  } else{
    db.deletePost(username, postid, function(del){
      if (!del){
        res.sendStatus(404);
      } else{
        res.sendStatus(204);
      }
    });
  }
});

router.post('/posts', function(req, res, next){
  let username = req.body.username;
  let postid = parseInt(req.body.postid);
  let title = req.body.title;
  let body = req.body.body;

  if (username === undefined|| postid === NaN || !title || !body){
    res.sendStatus(400);
  } else{
    if (typeof(title) != "string" || typeof(body) != "string" || typeof(username) != "string" || typeof(postid) != "number"){
      res.sendStatus(400);
    } else{
      if (postid == 0){
        db.insertPost(username, postid, title, body, function(insert){
          if (!insert){
            res.sendStatus(400);
          } else{
            res.status(201).send(insert);
          }
        });
      } else{
        db.updatePost(username, postid, title, body, function(insert){
          if (!insert){
            res.sendStatus(404);
          } else{
            res.status(200).send(insert);
          }
        });
      }
    }
  }
});

module.exports = router;
