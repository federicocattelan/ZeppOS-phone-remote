import { log as Logger } from "@zos/utils";
import { BaseApp } from "@zeppos/zml/base-app";

const logger = Logger.getLogger("phone-remote");

App(
  BaseApp({
    globalData: {},
    onCreate() {
      logger.log("Phone Remote watch app started");
    },
    onDestroy() {
      logger.log("Phone Remote watch app stopped");
    },
  })
);
