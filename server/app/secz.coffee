crypto = require 'crypto'
config = require '../config'

SECRET_CHARS = "ABCDEFGHIJKLMNPQRSTUVWXYZ123456789"

secz = module.exports =

  # SHA-1 + salt
  hashPwd: (pwd) ->
    digest = crypto.createHash 'sha1'
    digest.update config.secz.pwdSalt
    digest.update pwd
    return digest.digest 'hex'

  # generates a "random" 32 character hexadeciamal string
  generateToken: -> (Math.floor(Math.random()*16).toString(16) for i in [1..32]).join('')
  generateSecret: -> (SECRET_CHARS.charAt(Math.round(Math.random()*SECRET_CHARS.length)) for i in [1..10]).join('')
