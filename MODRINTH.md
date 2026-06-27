# QLogin

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20|%201.20.4%20|%201.21-blue?style=flat-square)
![Environment](https://img.shields.io/badge/Environment-Server%20Only-orange?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

A server-side Fabric login authentication system inspired by AuthMe. No client-side installation required.

**Features:**
- `/register` `/login` `/l` `/logout` `/changepassword`
- Admin: `/loginmod reload / unregister / resetpassword / info` (Tab-complete)
- Blocks movement, chat, interactions & commands before login
- Login timeout kick + IP ban on failed attempts
- SQLite storage, SHA-256 + Salt password hashing
- Colored messages + ActionBar countdown
- Supports 1.20.1 / 1.20.4 / 1.21
- Xinhua Network Link compatible

**Install:** Drop JAR into `mods/`, requires Fabric API. Server-side only.
