var createError = require('http-errors');
var express = require('express');
var path = require('path');
var cookieParser = require('cookie-parser');
var logger = require('morgan');
var commonmark = require('commonmark');

var app = express();

let editor = require('./routes/editor');
app.use('/editor', editor);

// required files
let mongo = require('./db');
let config = require('./config')
let index = require('./routes/index');
let blog = require('./routes/blog');
let login = require('./routes/login');
let posts = require('./routes/posts');

// view engine setup
app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(logger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use('/', index);
app.use('/blog', blog);
app.use('/login', login);
app.use('/api', posts);
app.use(express.static(path.join(__dirname, 'public')));



// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  err_status = err.status || 500;
  res.status(err_status);
  res.render('error', {code:err_status, message:"Error"});
});

module.exports = app;
