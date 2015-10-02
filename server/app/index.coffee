express = require 'express'
app = express()
bodyParser = require 'body-parser'
logger = require 'morgan'
routes = require './routes'
filters = require './filters'
cookieParser = require 'cookie-parser'

app.use logger('dev')
app.use cookieParser()
app.use bodyParser.urlencoded( extended: false )

app.use filter for _, filter of filters
app.use routes


# error handlers

# development error handler
# will print stacktrace
if app.get('env') == 'development'
  app.use (err, req, res, next) ->
    res.status(err.status || 500)
    res.send err

# production error handler
# no stacktraces leaked to user
app.use (err, req, res, next) ->
  res.status(err.status || 500)
  res.send "Error #{err.status}"


server = app.listen 3000, ->
  host = server.address().address
  port = server.address().port

  console.log "Example app listening at http://#{host}:#{port}"
