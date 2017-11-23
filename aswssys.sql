/*
Navicat MySQL Data Transfer

Source Server         : TMS
Source Server Version : 50528
Source Host           : 192.168.55.24:3306
Source Database       : aswssys

Target Server Type    : MYSQL
Target Server Version : 50528
File Encoding         : 65001

Date: 2017-11-22 10:46:38
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `cache_buglist`
-- ----------------------------
DROP TABLE IF EXISTS `cache_buglist`;
CREATE TABLE `cache_buglist` (
  `id` int(5) NOT NULL,
  `domain` varchar(100) DEFAULT NULL,
  `proj` varchar(100) DEFAULT NULL,
  `summary` varchar(500) DEFAULT NULL,
  `state` varchar(40) DEFAULT NULL,
  `severity` varchar(40) DEFAULT NULL,
  `tester` varchar(50) DEFAULT '',
  `assignto` varchar(50) DEFAULT '',
  `defect_time` datetime DEFAULT NULL,
  `module` varchar(30) DEFAULT NULL,
  `version` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `qa_attendance`
-- ----------------------------
DROP TABLE IF EXISTS `qa_attendance`;
CREATE TABLE `qa_attendance` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  `year` varchar(5) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `attendtype` varchar(24) DEFAULT NULL,
  `attendtime` varchar(30) DEFAULT NULL,
  `man_day` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=516 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `qa_contractors`
-- ----------------------------
DROP TABLE IF EXISTS `qa_contractors`;
CREATE TABLE `qa_contractors` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `name` varchar(30) DEFAULT NULL,
  `status` varchar(12) DEFAULT NULL,
  `sign_num` int(2) DEFAULT NULL,
  `entry_date` datetime DEFAULT NULL,
  `quit_date` datetime DEFAULT NULL,
  `period` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `qa_manpower`
-- ----------------------------
DROP TABLE IF EXISTS `qa_manpower`;
CREATE TABLE `qa_manpower` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `date` datetime DEFAULT NULL,
  `man_day` varchar(10) DEFAULT '0',
  `man_req` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=187 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `qa_month_tcexe`
-- ----------------------------
DROP TABLE IF EXISTS `qa_month_tcexe`;
CREATE TABLE `qa_month_tcexe` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `month` datetime DEFAULT NULL,
  `tcexe` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `qa_tasks`
-- ----------------------------
DROP TABLE IF EXISTS `qa_tasks`;
CREATE TABLE `qa_tasks` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `et_index` varchar(30) DEFAULT NULL,
  `proj` varchar(30) DEFAULT NULL,
  `subversion` varchar(20) DEFAULT NULL,
  `name` varchar(100) DEFAULT NULL,
  `executor` varchar(20) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `endtime_exp` datetime DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `precondition` varchar(400) NOT NULL,
  `content` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `qa_worklogs`
-- ----------------------------
DROP TABLE IF EXISTS `qa_worklogs`;
CREATE TABLE `qa_worklogs` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `create_time` datetime DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `name` varchar(30) DEFAULT NULL,
  `attendtime` varchar(30) DEFAULT NULL,
  `worktype` varchar(30) DEFAULT NULL,
  `content` varchar(200) DEFAULT NULL,
  `testproj` varchar(100) DEFAULT NULL,
  `projversion` varchar(20) DEFAULT NULL,
  `tcexe` int(3) DEFAULT NULL,
  `newbug` int(3) DEFAULT NULL,
  `regbug` int(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=559 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_prodline`
-- ----------------------------
DROP TABLE IF EXISTS `sys_prodline`;
CREATE TABLE `sys_prodline` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `product_line` varchar(100) DEFAULT NULL,
  `pl_owner` varchar(20) DEFAULT NULL,
  `pl_tag` varchar(3) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_products`
-- ----------------------------
DROP TABLE IF EXISTS `sys_products`;
CREATE TABLE `sys_products` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `product_line` varchar(100) DEFAULT NULL,
  `product` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_projects`
-- ----------------------------
DROP TABLE IF EXISTS `sys_projects`;
CREATE TABLE `sys_projects` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `proj_id` varchar(15) DEFAULT NULL,
  `proj_name` varchar(200) DEFAULT NULL,
  `product_line` varchar(100) DEFAULT NULL,
  `product` varchar(200) DEFAULT NULL,
  `customer` varchar(80) DEFAULT NULL,
  `proj_status` varchar(40) DEFAULT NULL,
  `approver` varchar(20) DEFAULT NULL,
  `priority` varchar(40) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `plan_end_time` datetime DEFAULT NULL,
  `responsor` varchar(20) DEFAULT NULL,
  `pm` varchar(20) DEFAULT NULL,
  `team_member` varchar(200) DEFAULT NULL,
  `git` varchar(50) DEFAULT NULL,
  `goals` varchar(500) DEFAULT NULL,
  `comments` varchar(500) DEFAULT NULL,
  `approtype` varchar(20) DEFAULT NULL,
  `creator` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_qualityreport`
-- ----------------------------
DROP TABLE IF EXISTS `sys_qualityreport`;
CREATE TABLE `sys_qualityreport` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `PQR_index` varchar(200) DEFAULT NULL,
  `PQR_name` varchar(50) DEFAULT NULL,
  `Fyear` varchar(5) DEFAULT NULL,
  `Fmonth` varchar(5) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `author` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_submit`
-- ----------------------------
DROP TABLE IF EXISTS `sys_submit`;
CREATE TABLE `sys_submit` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `TSB_index` varchar(300) DEFAULT NULL,
  `proj` varchar(30) DEFAULT NULL,
  `version` varchar(20) DEFAULT NULL,
  `subversion` varchar(20) DEFAULT NULL,
  `pm` varchar(30) DEFAULT NULL,
  `Submittime` datetime DEFAULT NULL,
  `Submitter` varchar(30) DEFAULT NULL,
  `Developer` varchar(200) DEFAULT NULL,
  `FunctionDes` varchar(800) DEFAULT NULL,
  `TRange` varchar(800) DEFAULT NULL,
  `Note` varchar(200) DEFAULT NULL,
  `UED_codeurl` varchar(200) DEFAULT NULL,
  `front_codeurl` varchar(200) DEFAULT NULL,
  `Other_codeurl` varchar(200) DEFAULT NULL,
  `Wikiurl` varchar(200) DEFAULT NULL,
  `Status` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_submit_pt`
-- ----------------------------
DROP TABLE IF EXISTS `sys_submit_pt`;
CREATE TABLE `sys_submit_pt` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `TSB_index` varchar(300) DEFAULT NULL,
  `proj` varchar(30) DEFAULT NULL,
  `version` varchar(20) DEFAULT NULL,
  `subversion` varchar(20) DEFAULT NULL,
  `pm` varchar(30) DEFAULT NULL,
  `Submittime` datetime DEFAULT NULL,
  `Submitter` varchar(30) DEFAULT NULL,
  `Developer` varchar(200) DEFAULT NULL,
  `Status` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_test_proj`
-- ----------------------------
DROP TABLE IF EXISTS `sys_test_proj`;
CREATE TABLE `sys_test_proj` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `proj` varchar(100) DEFAULT NULL,
  `version` varchar(20) DEFAULT NULL,
  `productline` varchar(30) DEFAULT NULL,
  `pm` varchar(30) DEFAULT NULL,
  `expectsttime` datetime DEFAULT NULL,
  `starttime` datetime DEFAULT NULL,
  `responsor` varchar(30) DEFAULT NULL,
  `projstatus` varchar(10) DEFAULT NULL,
  `priority` int(3) DEFAULT NULL,
  `testengineer` varchar(200) DEFAULT NULL,
  `teststatus` varchar(10) DEFAULT NULL,
  `timecost` int(3) DEFAULT NULL,
  `cycle` int(2) DEFAULT NULL,
  `relativor` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_test_task`
-- ----------------------------
DROP TABLE IF EXISTS `sys_test_task`;
CREATE TABLE `sys_test_task` (
  `id` int(6) NOT NULL AUTO_INCREMENT,
  `ST_index` varchar(300) DEFAULT NULL,
  `proj` varchar(30) DEFAULT NULL,
  `version` varchar(20) DEFAULT NULL,
  `subversion` varchar(20) DEFAULT NULL,
  `pm` varchar(30) DEFAULT NULL,
  `starttime` datetime DEFAULT NULL,
  `endtime` datetime DEFAULT NULL,
  `responsor` varchar(30) DEFAULT NULL,
  `teststatus` varchar(10) DEFAULT NULL,
  `timecost` int(3) DEFAULT NULL,
  `cycle` int(2) DEFAULT NULL,
  `testtype` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_testreport`
-- ----------------------------
DROP TABLE IF EXISTS `sys_testreport`;
CREATE TABLE `sys_testreport` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `TR_index` varchar(300) DEFAULT NULL,
  `productline` varchar(30) DEFAULT NULL,
  `proj` varchar(30) DEFAULT NULL,
  `version` varchar(20) DEFAULT NULL,
  `subversion` varchar(20) DEFAULT NULL,
  `Tr_time` datetime DEFAULT NULL,
  `Reporter` varchar(30) DEFAULT NULL,
  `testresult` varchar(10) DEFAULT NULL,
  `Num_tc_exe` int(5) DEFAULT NULL,
  `Num_tc_fail` int(5) DEFAULT NULL,
  `Num_bug_unclose` int(5) DEFAULT NULL,
  `Num_bug_total` int(5) DEFAULT NULL,
  `Num_bug_ul3` int(5) DEFAULT NULL,
  `Num_bug_reopen` int(5) DEFAULT NULL,
  `Num_bug_close` int(5) DEFAULT NULL,
  `Num_dqs` varchar(10) DEFAULT NULL,
  `Rate_bug_open` varchar(10) DEFAULT NULL,
  `Rate_bug_reopen` varchar(10) DEFAULT NULL,
  `Rate_tc_fail` varchar(10) DEFAULT NULL,
  `Time_start` datetime DEFAULT NULL,
  `Time_end` datetime DEFAULT NULL,
  `Timecost` int(3) DEFAULT NULL,
  `Cycles` int(2) DEFAULT NULL,
  `Responsor` varchar(30) DEFAULT NULL,
  `Testengineer` varchar(200) DEFAULT NULL,
  `issues` varchar(500) DEFAULT NULL,
  `TRange` varchar(500) DEFAULT NULL,
  `Testconditions` varchar(400) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_usr_purview`
-- ----------------------------
DROP TABLE IF EXISTS `sys_usr_purview`;
CREATE TABLE `sys_usr_purview` (
  `id` int(5) NOT NULL AUTO_INCREMENT,
  `usrname` varchar(30) NOT NULL,
  `weekreport_Page_Access` varchar(80) DEFAULT 'y',
  `weekreport_AddTWR` varchar(80) DEFAULT 'x',
  `weekreport_CheckTWR` varchar(80) DEFAULT 'y',
  `weekreport_RemoveTWR` varchar(80) DEFAULT 'x',
  `RDcloud_Page_Access` varchar(80) DEFAULT 'x',
  `ReqMan_Page_Access` varchar(80) DEFAULT 'y',
  `ReqMan_Add` varchar(80) DEFAULT 'x',
  `ReqMan_Del` varchar(80) DEFAULT 'x',
  `ReqMan_Import` varchar(80) DEFAULT 'x',
  `projlist_Page_Access` varchar(80) DEFAULT 'y',
  `projlist_ProductLine` varchar(80) DEFAULT 'x',
  `projlist_Product` varchar(80) DEFAULT 'x',
  `projlist_AddProj` varchar(80) DEFAULT 'x',
  `projlist_UpdateProj` varchar(80) DEFAULT 'x',
  `projlist_CloseProj` varchar(80) DEFAULT 'x',
  `projlist_DelProj` varchar(80) DEFAULT 'x',
  `projlist_ApprovProj` varchar(80) DEFAULT 'x',
  `TP_list_Page_Access` varchar(80) DEFAULT 'y',
  `TP_list_Addtp` varchar(80) DEFAULT 'x',
  `TP_list_Closetp` varchar(80) DEFAULT 'x',
  `TP_list_Submit` varchar(80) DEFAULT 'x',
  `TP_list_CheckSubmit` varchar(80) DEFAULT 'y',
  `TP_list_ApprovSubmit` varchar(80) DEFAULT 'x',
  `TP_list_AddTR` varchar(80) DEFAULT 'x',
  `TP_list_ApprovTR` varchar(80) DEFAULT 'x',
  `TP_list_CheckTR` varchar(80) DEFAULT 'y',
  `TestExce_Page_Access` varchar(80) DEFAULT 'y',
  `TestExce_Contractor` varchar(80) DEFAULT 'x',
  `TestExce_Attendee` varchar(80) DEFAULT 'x',
  `TestExce_AddTask` varchar(80) DEFAULT 'x',
  `TestExce_UpdateTask` varchar(80) DEFAULT 'x',
  `TestExce_DelTask` varchar(80) DEFAULT 'x',
  `TestExce_ManReq` varchar(80) DEFAULT 'x',
  `TestExce_AddDlog` varchar(80) DEFAULT 'x',
  `TestExce_UpdateDlog` varchar(80) DEFAULT 'x',
  `TestExce_DelDlog` varchar(80) DEFAULT 'x',
  `TestExce_ImportDlog` varchar(80) DEFAULT 'x',
  `PQR_Page_Access` varchar(80) DEFAULT 'y',
  `PQR_Add` varchar(80) DEFAULT 'x',
  `PQR_Del` varchar(80) DEFAULT 'x',
  `PQR_Import` varchar(80) DEFAULT 'y',
  `Log_verify_Page_Access` varchar(80) DEFAULT 'y',
  `PerformanceTest_Page_Access` varchar(80) DEFAULT 'y',
  `UsrMange_Page_Access` varchar(80) DEFAULT 'x',
  `Purview_Page_Access` varchar(80) DEFAULT 'x',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=330 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_usr_purview
-- ----------------------------
INSERT INTO `sys_usr_purview` VALUES ('1', 'init0', '页面访问', '创建团队周报', '查看团队周报', '删除团队周报', '页面访问', '页面访问', '创建需求', '删除需求', '导出需求', '页面访问', '产品线管理', '产品管理', '创建项目', '项目变更', '关闭项目', '删除项目', '项目审批', '页面访问', '创建测试项目', '关闭测试项目', '项目提测', '查看提测', '提测审核', '创建测试报告', '审核测试报告', '查看测试报告', '页面访问', '实习生管理', '考勤数据编辑', '创建任务', '修改任务', '删除任务', '实习生需求数据编辑', '填写日志', '修改日志', '删除日志', '导出日志', '页面访问', '创建质量报告', '删除质量报告', '导出质量报告', '页面访问', '页面访问', '页面访问', '页面访问');
INSERT INTO `sys_usr_purview` VALUES ('2', 'admin', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y', 'y');
INSERT INTO `sys_usr_purview` VALUES ('3', 'guest', 'y', 'x', 'y', 'x', 'x', 'y', 'x', 'x', 'x', 'y', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'y', 'x', 'x', 'x', 'y', 'x', 'x', 'x', 'y', 'y', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'x', 'y', 'x', 'x', 'y', 'y', 'y', 'x', 'x');

-- ----------------------------
-- Table structure for `sys_usr_role`
-- ----------------------------
DROP TABLE IF EXISTS `sys_usr_role`;
CREATE TABLE `sys_usr_role` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `usrname` varchar(30) DEFAULT NULL,
  `fullname` varchar(40) DEFAULT NULL,
  `dept1` varchar(50) DEFAULT '',
  `dept2` varchar(50) DEFAULT '',
  `role` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `sys_usrdb`
-- ----------------------------
DROP TABLE IF EXISTS `sys_usrdb`;
CREATE TABLE `sys_usrdb` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `usrname` varchar(30) DEFAULT NULL,
  `passwd` varchar(40) DEFAULT NULL,
  `fullname` varchar(40) DEFAULT NULL,
  `dept1` varchar(50) DEFAULT '',
  `dept2` varchar(50) DEFAULT '',
  `dept3` varchar(50) DEFAULT '',
  `role` varchar(10) DEFAULT NULL,
  `email` varchar(30) DEFAULT NULL,
  `mobile` varchar(15) DEFAULT NULL,
  `privil` varchar(3) DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sys_usrdb
-- ----------------------------
INSERT INTO `sys_usrdb` VALUES ('1', 'admin', 'fce', '系统管理员', '', '', '', 'sysadmin', 'lihao@auto-smart.com', '', '0', 'local');
INSERT INTO `sys_usrdb` VALUES ('2', 'guest', 'fce', '访客', '', '', '', 'user', 'aaa@163.com', '123', '0', 'local');

-- ----------------------------
-- Table structure for `sys_weekreport`
-- ----------------------------
DROP TABLE IF EXISTS `sys_weekreport`;
CREATE TABLE `sys_weekreport` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `wr_index` varchar(400) DEFAULT NULL,
  `fname` varchar(400) DEFAULT NULL,
  `week_date` varchar(100) DEFAULT NULL,
  `type` varchar(30) DEFAULT NULL,
  `owner` varchar(200) DEFAULT NULL,
  `year` varchar(30) DEFAULT NULL,
  `week` varchar(30) DEFAULT NULL,
  `author` varchar(200) DEFAULT NULL,
  `creattime` datetime DEFAULT NULL,
  `complete` varchar(100) DEFAULT '',
  `status` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `vmm_ippool`
-- ----------------------------
DROP TABLE IF EXISTS `vmm_ippool`;
CREATE TABLE `vmm_ippool` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `network` varchar(20) DEFAULT '',
  `net` varchar(20) DEFAULT '',
  `addr` varchar(20) DEFAULT '',
  `state` varchar(20) DEFAULT '可用',
  `des` varchar(200) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=509 DEFAULT CHARSET=utf8;


-- ----------------------------
-- Table structure for `vmm_network`
-- ----------------------------
DROP TABLE IF EXISTS `vmm_network`;
CREATE TABLE `vmm_network` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `network` varchar(20) DEFAULT '',
  `gateway` varchar(20) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `vmm_phy_server`
-- ----------------------------
DROP TABLE IF EXISTS `vmm_phy_server`;
CREATE TABLE `vmm_phy_server` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `hostname` varchar(20) DEFAULT '',
  `ipaddr` varchar(20) DEFAULT '',
  `locate` varchar(200) DEFAULT '',
  `sn` varchar(20) DEFAULT '',
  `model` varchar(20) DEFAULT '',
  `asset_sn` varchar(20) DEFAULT '',
  `vm_num` varchar(3) DEFAULT '',
  `cpu` varchar(3) DEFAULT '',
  `cpu_used` varchar(3) DEFAULT '',
  `cpu_usable` varchar(3) DEFAULT '',
  `cpu_rate_usage` varchar(10) DEFAULT '',
  `mem` varchar(5) DEFAULT '',
  `mem_used` varchar(5) DEFAULT '',
  `mem_usable` varchar(5) DEFAULT '',
  `mem_rate_usage` varchar(10) DEFAULT '',
  `disk` varchar(10) DEFAULT '',
  `disk_used` varchar(10) DEFAULT '',
  `disk_usable` varchar(10) DEFAULT '',
  `disk_rate_usage` varchar(10) DEFAULT '',
  `passwd` varchar(100) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `vmm_vm`
-- ----------------------------
DROP TABLE IF EXISTS `vmm_vm`;
CREATE TABLE `vmm_vm` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `hostname` varchar(20) DEFAULT '',
  `name` varchar(20) DEFAULT '',
  `os` varchar(200) DEFAULT '',
  `cpu` varchar(10) DEFAULT '',
  `mem` varchar(10) DEFAULT '',
  `disk` varchar(10) DEFAULT '',
  `ip` varchar(20) DEFAULT '',
  `des` varchar(200) DEFAULT '',
  `user` varchar(20) DEFAULT '',
  `type` varchar(20) DEFAULT '',
  `passwd` varchar(200) DEFAULT '',
  `note` varchar(200) DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8;