import * as hmUI from "@zos/ui";
import { log as Logger } from "@zos/utils";
import { showToast } from "@zos/interaction";
import { BasePage } from "@zeppos/zml/base-page";
import {
  STATUS_TEXT,
  WAZE_BUTTON,
  GPS_BUTTON,
  GPS_ON_SRC,
  GPS_OFF_SRC,
} from "./index.r.layout.js";

const logger = Logger.getLogger("page");

Page(
  BasePage({
    state: {
      statusWidget: null,
      wazeButton: null,
      gpsButton: null,
      gpsEnabled: false,
      gpsKnown: false,
    },
    build() {
      logger.log("page build invoked");

      this.state.statusWidget = hmUI.createWidget(hmUI.widget.TEXT, {
        ...STATUS_TEXT,
        text: "Phone Remote",
      });

      this.state.wazeButton = hmUI.createWidget(hmUI.widget.BUTTON, {
        ...WAZE_BUTTON,
        click_func: () => this.launchWaze(),
      });

      this.state.gpsButton = hmUI.createWidget(hmUI.widget.BUTTON, {
        ...GPS_BUTTON,
        ...GPS_OFF_SRC,
        text: "GPS: ...",
        click_func: () => this.toggleGps(),
      });

      this.checkHealth();
    },
    onInit() {
      logger.log("page onInit invoked");
    },
    onDestroy() {
      logger.log("page onDestroy invoked");
    },
    setStatus(message) {
      this.state.statusWidget.setProperty(hmUI.prop.TEXT, message);
    },
    renderGpsButton() {
      const src = this.state.gpsEnabled ? GPS_ON_SRC : GPS_OFF_SRC;
      const label = this.state.gpsKnown
        ? `GPS: ${this.state.gpsEnabled ? "ON" : "OFF"}`
        : "GPS";

      // BUTTON widgets require x/y/w/h on every setProperty(MORE) call,
      // and normal_src/press_src can only be changed via direct assignment
      // (setProperty does not support them for this widget type).
      this.state.gpsButton.setProperty(hmUI.prop.MORE, {
        x: GPS_BUTTON.x,
        y: GPS_BUTTON.y,
        w: GPS_BUTTON.w,
        h: GPS_BUTTON.h,
        text: label,
      });
      this.state.gpsButton.normal_src = src.normal_src;
      this.state.gpsButton.press_src = src.press_src;
    },
    checkHealth() {
      this.request({
        method: "HEALTH_CHECK",
      })
        .then((result) => {
          if (result && typeof result.gpsEnabled === "boolean") {
            this.state.gpsEnabled = result.gpsEnabled;
            this.state.gpsKnown = true;
            this.renderGpsButton();
            this.setStatus("Pronto");
          } else {
            this.setStatus("Companion non raggiungibile");
          }
        })
        .catch((error) => {
          logger.log("HEALTH_CHECK failed", error);
          this.setStatus("Companion non raggiungibile");
        });
    },
    launchWaze() {
      this.setStatus("Apro Waze...");

      this.request({
        method: "EXECUTE_SHORTCUT",
        params: { action: "launch_waze" },
      })
        .then((result) => {
          const message =
            (result && result.message) ||
            (result && result.success ? "Waze avviato" : "Errore");
          this.setStatus(message);
          showToast({ content: message });
        })
        .catch(() => {
          this.setStatus("Companion non raggiungibile");
        });
    },
    toggleGps() {
      const nextAction = this.state.gpsEnabled ? "gps_off" : "gps_on";
      this.setStatus("Aggiorno GPS...");

      this.request({
        method: "EXECUTE_SHORTCUT",
        params: { action: nextAction },
      })
        .then((result) => {
          if (result && typeof result.enabled === "boolean") {
            this.state.gpsEnabled = result.enabled;
            this.state.gpsKnown = true;
          } else if (result && result.success) {
            this.state.gpsEnabled = nextAction === "gps_on";
            this.state.gpsKnown = true;
          }

          this.renderGpsButton();

          const message =
            (result && result.message) ||
            (result && result.success ? "Fatto" : "Errore");
          this.setStatus(message);
          showToast({ content: message });
        })
        .catch(() => {
          this.setStatus("Companion non raggiungibile");
        });
    },
  })
);
