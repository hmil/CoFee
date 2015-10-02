express = require 'express'
router = express.Router()
db = require './db'
secz = require './secz'

router.post '/user/create', (req, res, next) ->
  return res.send "Invalid parameters", 400 if !req.body.user? or !req.body.pwd?
  cryptedPwd = secz.hashPwd req.body.pwd
  db.query "INSERT INTO `users`(`nick`, `pwd`) VALUES(?, ?)",
    [req.body.user, cryptedPwd],
    (err, rows, fields) ->
      if err
        if err.code == "ER_DUP_ENTRY"
          res.send "ERROR:err_nick_taken:This nickname is already taken", 400
        else
          next(err)
      else
        res.send("registration ok")

router.post '/user/login', (req, res, next) ->
  return res.send "Invalid parameters", 400 if !req.body.user? or !req.body.pwd?
  cryptedPwd = secz.hashPwd req.body.pwd

  db.query "SELECT id FROM `users` WHERE `nick`=? AND `pwd`=?",
    [req.body.user, cryptedPwd],
    (err, rows, fields) ->
      if err
        next(err)
      else if rows.length == 0
        res.send "ERROR: invalid username or password", 400
      else
        token = secz.generateToken()
        id = rows[0].id
        db.query "INSERT INTO `userTokens`(`token`, `user_id`) VALUES(?, ?)",
          [token, id],
          (err, rows, fields) ->
            return next(err) if err?
            res.cookie 'token', token, {maxAge: 3600*24*365, httpOnly: true}
            res.send token

router.get '/counts', (req, res, next) ->
  db.query "SELECT counts.id, counts.name FROM users_counts LEFT JOIN counts ON counts.id = users_counts.count_id WHERE user_id=?",
    [req.userId],
    (err, rows, fields) ->
      if err
        next(err)
      else
        res.send rows

router.post '/counts/subscribe', (req, res, next) ->

  return res.send "Invalid parameters", 400 if !req.body.secret?
  db.query "SELECT id from counts WHERE secret=?",
    [req.body.secret],
    (err, rows, fields) ->
      if err
        next(err)
      else if rows.length == 0
        res.send "ERROR: no such count", 400
      else
        db.query "INSERT INTO users_counts(user_id, count_id) VALUES(?, ?)",
          [req.userId, rows[0].id],
          (err, rows, fields) ->
            if err
              next(err)
            else
              res.send "ok", 200

router.post '/counts/create', (req, res, next) ->
  return res.send "Invalid parameters", 400 if !req.body.name?
  secret = secz.generateSecret()
  db.query "INSERT INTO counts(name, secret) VALUES(?, ?)",
    [req.body.name, secret],
    (err, rows, fields) ->
      if err
        next(err)
      else
        countId = rows.insertId
        db.query "INSERT INTO users_counts(user_id, count_id) VALUES(?, ?)",
        [req.userId, countId],
        (err, rows, fields) ->
          if err
            next(err)
          else
            res.send {id: countId}

router.get '/count/:secret', (req, res) ->
  res.send("TODO: authentication middleware + read this count")


module.exports = router
