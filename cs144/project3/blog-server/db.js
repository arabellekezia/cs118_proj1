const MongoClient = require('mongodb').MongoClient;
const config = require('./config');
const options = { useUnifiedTopology: true, writeConcern: { j: true } };
let client = null;
var db;

MongoClient.connect(config.mongoURL, function(err, client) {
  if (err){
    console.log("Unable to connect to Mongo");
  } else{
    db = client.db(config.db);
    console.log("Connected to Mongo");
  }
});

var getMaxId = function(username, callback){
  if (username){
    db.collection('Users').findOne({"username":username}, function(err, user){
      if (err != null){
        callback(err);
      } else{
        if (user == null){
          callback(user);
        } else{
          callback(user.maxid);
        }
      }
    });
  }
};


// export connect(), db() and close() from the module
module.exports = {
    _db:this.db,

    getPost: function(username, postid, callback){
      var query = {
        $and:[
          {"postid":postid},
          {"username":username}
        ]
      };
      db.collection('Posts').findOne(query, function(err, res){
        if (err != null){
          callback(err);
        } else{
          callback(res);
        }
      });
    },

    getPosts: function(username, start, callback){
      var query = {
        "username":username,
        "postid":{$gte:start}
      };
      db.collection('Posts').find(query).toArray(function(err, res){
        if (err != null){
          callback(err);
        } else{
          callback(res);
        }
      });
    },

    getPassword: function(username, callback){
      var query = {"username":username};
      // var proj = {projection: {"_id":0, "username":0, "maxid":0, "password":1}};
      db.collection('Users').findOne(query, function(err,res){
        if (err != null){
          callback(err);
        } else{
          callback(res);
        }
      });
    },

    getPosts2: function(username, callback){
      var query = {"username":username};
      db.collection('Posts').find(query).toArray(function(err, res){
        if (err != null){
          callback(err);
        } else{
          callback(res);
        }
      });
    },

    deletePost: function(username, postid, callback){
      var query = {
        "username":username,
        "postid":postid
      };
      getMaxId(username, function(res){
        if (postid == res){
          let new_max = res - 1;
          console.log("New max: " + new_max);
          db.collection('Users').updateOne({"username":username}, {$set:{"maxid":new_max}});
        }
      });
      db.collection('Posts').deleteOne(query, function(err, res){
        if (err != null){
          callback(err);
        } else{
          callback(res.result.n);
        }
      });
    },

    // insert new blog for when postid=0
    insertPost: function(username, postid, title, body, callback){
      var time_now = new Date().getTime();
      getMaxId(username, function(res){
        if(res){
          var new_id = res + 1;
          var query = {
            "postid":new_id,
            "username":username,
            "created":time_now,
            "modified":time_now,
            "title":title,
            "body":body
          };
          db.collection('Posts').insertOne(query, function(err, insert){
            if (err != null){
              callback(err);
            } else{
              if (insert.result.n != 0){
                var ret = {
                  "postid":new_id,
                  "created":time_now,
                  "modified":time_now
                };
                db.collection('Users').updateOne({"username":username}, {$set:{"maxid":new_id}});
                callback(ret);
              } else{
                callback(false);
              }
            }
          });
        } else{
          callback(false);
        }
      });



    },

    // update post for when postid > 0
    updatePost: function(username, postid, title, body, callback){
      var time_now = new Date().getTime();
      var query = {
        "username":username,
        "postid":postid
      };
      var update = {
        $set:{
          "title":title,
          "body":body,
          "modified":time_now
        }
      };
      db.collection('Posts').updateOne(query, update, function(err, ret){
        if (err != null){
          callback(err);
        } else{
          if (ret.result.nModified != 0){
            var ret = { "modified":time_now };
            callback(ret);
          } else{
            callback(false);
          }
        }
      });

    }

};
