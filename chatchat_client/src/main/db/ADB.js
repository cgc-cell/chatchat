const fs = require('fs')
const os = require('os')
import { add_tables, add_index, alter_tables } from './Table'
//轻量级的数据库驱动
const sqlite3 = require('sqlite3').verbose()
const NODE_ENV = process.env.NODE_ENV

//获取用户目录
const userDir = os.homedir()
// console.log('用户目录:', userDir)
//创建数据库缓存文件夹
const dbFolder = userDir + `${NODE_ENV === 'development' ? '/.testChatChat/' : '/.ChatChat/'}`
// console.log('数据库文件夹:', dbFolder)
if (!fs.existsSync(dbFolder)) {
  fs.mkdirSync(dbFolder)
}
//创建数据库文件
const db = new sqlite3.Database(dbFolder + 'localChatChat.db')
//循环执行sql语句
const createTable = () => {
  return new Promise((resolve, reject) => {
    const runAsyncTasks = async () => {
      try {
        for (const item of add_tables) {
          await db.run(item)
        }
        for (const item of add_index) {
          await db.run(item)
        }
        for (const item of alter_tables) {
          //PRAGMA table_info(table_name)查询表的所有字段信息
          const fieldList = await queryAll(`PRAGMA table_info(${item.tableName})`, [])
          const field = fieldList.some((row) => row.name === item.field)
          if (!field) {
            await db.run(item.sql)
          }
        }
        resolve()
      } catch (error) {
        reject(error)
      }
    }
    runAsyncTasks()
  })
}

//查询所需数据处理后进行返回
const queryAll = (sql, params) => {
  return new Promise((resolve) => {
    const stmt = db.prepare(sql) //预处理sql语句
    //执行sql，进行语句格式修改后进行返回处理
    stmt.all(params, function (err, rows) {
      if (err) {
        resolve([])
      }
      rows.forEach((item, index) => {
        rows[index] = covertDbObj2BizObj(item)
      })
      resolve(rows)
    })
    stmt.finalize()
  })
}

//改变从数据库到使用的数据格式
const covertDbObj2BizObj = (data) => {
  if (!data) {
    return null
  }
  const bizData = {}
  for (let item in data) {
    bizData[toCamelCase(item)] = data[item]
  }
  return bizData
}

//正则表达的规则改写
const toCamelCase = (str) => {
  return str.replace(/_([a-z])/g, function (match, p1) {
    return String.fromCharCode(p1.charCodeAt(0) - 32)
  })
}
//业务字段到数据库字段的映射
const globalColumnsMap = {}
const initTableColumnsMap = async () => {
  let sql = `select name from sqlite_master where type='table' AND name !='sqlite_sequence'`
  let tables = await queryAll(sql, [])
  for (let i = 0; i < tables.length; i++) {
    sql = `PRAGMA table_info(${tables[i].name})`
    let columns = await queryAll(sql, [])
    const columnMapItem = {}
    for (let j = 0; j < columns.length; j++) {
      columnMapItem[toCamelCase(columns[j].name)] = columns[j].name
    }
    globalColumnsMap[tables[i].name] = columnMapItem
  }
}
//查询单个数据
const queryCount = (sql, params) => {
  return new Promise((resolve) => {
    const stmt = db.prepare(sql)
    stmt.get(params, function (err, row) {
      if (err) {
        return resolve(0)
      }
      resolve(Array.from(Object.values(row))[0])
    })
    stmt.finalize()
  })
}
//单条语句的查询
const queryOne = (sql, params) => {
  return new Promise((resolve) => {
    const stmt = db.prepare(sql)
    stmt.get(params, function (err, row) {
      if (err) {
        resolve(null)
      }
      // console.log(`执行的sql语句:${sql},参数(params):${params},结果(row):${JSON.stringify(row)}`)
      resolve(covertDbObj2BizObj(row))
    })
    stmt.finalize()
  })
}
//执行sql语句（被调用的方法）
const run = (sql, params) => {
  return new Promise((resolve) => {
    const stmt = db.prepare(sql)
    stmt.run(params, function (err) {
      if (err) {
        resolve('数据库操作失败')
        console.log(`执行的sql语句:${sql},参数(params):${params},数据库操作失败`)
      } else {
        console.log(`执行的sql语句:${sql},参数(params):${params},执行记录数:${this.changes}`)
      }
      resolve(this.changes)
    })
  })
}
//执行插入语句(执行的方法)
const insert = (sqlPrefix, tableName, data) => {
  const columnsMap = globalColumnsMap[tableName]
  const dbColumns = []
  const params = []
  for (let item in data) {
    if (data[item] !== undefined && data[item] !== null && columnsMap[item] !== undefined) {
      dbColumns.push(columnsMap[item])
      params.push(data[item])
    }
  }
  const preper = '?'.repeat(dbColumns.length).split('').join(',')
  const sql = `${sqlPrefix} ${tableName} (${dbColumns.join(',')}) values (${preper})`
  return run(sql, params)
}
//执行插入或者替换语句
const insertOrReplace = (tableName, data) => {
  return insert('insert or replace into', tableName, data)
}
//执行插入或者忽略语句
const insertOrIgnore = (tableName, data) => {
  return insert('insert or ignore into', tableName, data)
}
//执行更新语句
const update = (tableName, data, paramData) => {
  const columnsMap = globalColumnsMap[tableName]
  const dbColumns = []
  const params = []
  const whereColumns = []
  for (let item in data) {
    if (data[item] !== undefined && data[item] !== null && columnsMap[item] !== undefined) {
      dbColumns.push(`${columnsMap[item]} = ?`)
      params.push(data[item])
    }
  }
  for (let item in paramData) {
    if (paramData[item]) {
      params.push(paramData[item])
      whereColumns.push(`${columnsMap[item]} = ?`)
    }
  }
  const sql = `update ${tableName} set ${dbColumns.join(',')} ${whereColumns.length > 0 ? 'where ' : ' '} ${whereColumns.join(' and ')}`
  return run(sql, params)
}
//所有方法准备就绪开始创建
const init = () => {
  db.serialize(async () => {
    await createTable()
    await initTableColumnsMap()
  })
}
init()
export { run, queryAll, queryOne, queryCount, insertOrReplace, insertOrIgnore, update }
