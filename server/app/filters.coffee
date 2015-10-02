db = require './db'

authFilter = (paths) ->
  pathRxs = paths.map (r) -> new RegExp(r)
  (req, res, next) ->
    for rx in pathRxs
      if rx.test(req.path)
        if !req.cookies['token']?
          res.send "auth error: no token", 400
          return
        db.query "SELECT user_id FROM `userTokens` WHERE `token`=?",
          [req.cookies['token']],
          (err, rows, fields) ->
            next(err) if err
            if rows.length != 1
              res.cookie 'token', '', {maxAge: 3600*24*365, httpOnly: true}
              res.send "auth error: wrong token", 400
              return
            req.userId = rows[0].user_id;
            next()
        return
    next()


filters = module.exports =
  auth: authFilter ['^/counts?']
