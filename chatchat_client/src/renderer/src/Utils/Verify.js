const regs = {
  email: /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/, //邮箱规则：邮箱名@域名
  number: /^\+?[1-9][0-9]*$/, //正整数
  password: /^(?=.*\d)(?=.*[a-zA-Z])[\da-zA-Z~!@#$%^&*_]{8,}$/, //密码规则：8位以上，包含数字和字母
  version: /^[0-9]+$/ //版本号规则：纯数字
}

const verify = (rule, value, reg, callback) => {
  if (value) {
    if (reg.test(value)) {
      callback()
    } else {
      callback(new Error(rule.message))
    }
  } else {
    callback()
  }
}
//检查输入密码是否符合规则
const checkPassword = (value) => {
  return regs.password.test(value)
}
//检查输入邮箱是否符合规则
const checkEmail = (value) => {
  return regs.email.test(value)
}
//进行表单验证时检查密码输入是否符合规则
const password = (rule, value, callback) => {
  return verify(rule, value, regs.password, callback)
}
//进行表单验证时检查邮箱输入是否符合规则
const email = (rule, value, callback) => {
  return verify(rule, value, regs.email, callback)
}
export default {
  checkPassword,
  checkEmail,
  password,
  email
}
