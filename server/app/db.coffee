config = require '../config.json'
mysql = require 'mysql'
connection = mysql.createConnection config.db
connection.connect()

module.exports = connection
