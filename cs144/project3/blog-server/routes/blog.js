var express = require('express');
var url = require('url');
var db = require("../db")
var commonmark = require('commonmark');
var parser = new commonmark.Parser();
var renderer = new commonmark.HtmlRenderer();

var router = express.Router();

// GET: get a blog post given username and post id
router.get('/:username/:postid', function(req, res, next){
  db.getPost(req.params.username, parseInt(req.params.postid), function(post){
    if(post != null){
      post.title = renderer.render(parser.parse(post.title));
      post.body = renderer.render(parser.parse(post.body));
      post.postid = post.postid.toString();
      post.created = new Date(post.created).toString();
      post.modified = new Date(post.modified).toString();
      res.status(200);
      res.render('post', {post:post});
    } else{
      res.status(404);
      res.render('error', {code:"404", message:"Username or post ID not found"});
    }
  });
});


// GET: get blog all blog posts given username
router.get('/:username', function(req, res, next){
  let start = parseInt(req.query.start);
  if (!start){
    start = 1;
  }
  db.getPosts(req.params.username, start, function(posts){
    if (posts.length == 0){
      res.status(404);
      res.render('error', {code:"404", message:"Username not found"});
    } else{
      next = '';
      if (posts.length > 5){
        next = posts[0].username + '?start=' + (start + 5);
        posts = posts.slice(0,5);
      }
      for (let post of posts){
        post.username = post.username;
        post.title = renderer.render(parser.parse(post.title));
        post.body = renderer.render(parser.parse(post.body));
        post.postid = post.postid.toString();
        post.created = new Date(post.created).toString();
        post.modified = new Date(post.modified).toString();
      }
      res.status(200);
      res.render('posts', {posts:posts, next:next});
    }
  });
});

module.exports = router;
