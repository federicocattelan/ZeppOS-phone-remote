import * as hmUI from "@zos/ui";
import { px } from "@zos/utils";
import { DEVICE_WIDTH } from "../utils/config/device";

// Layout is hand-tuned for the 466x466 round Active 2 display so that every
// widget's bounding box stays inside the visible circle: wider near the
// vertical center, progressively narrower (curve-trimmed) further from it.
export const STATUS_TEXT = {
  x: px(62),
  y: px(100),
  w: px(342),
  h: px(36),
  text_size: px(26),
  align_h: hmUI.align.CENTER_H,
  align_v: hmUI.align.CENTER_V,
  color: 0xffffff,
};

export const WAZE_BUTTON = {
  x: px(37),
  y: px(144),
  w: px(392),
  h: px(104),
  text: "Waze",
  text_size: px(34),
  color: 0xffffff,
  normal_src: "waze_normal.png",
  press_src: "waze_press.png",
};

export const GPS_BUTTON = {
  x: px(62),
  y: px(262),
  w: px(342),
  h: px(104),
  text_size: px(30),
  color: 0xffffff,
};

export const GPS_ON_SRC = {
  normal_src: "gps_on_normal.png",
  press_src: "gps_on_press.png",
};

export const GPS_OFF_SRC = {
  normal_src: "gps_off_normal.png",
  press_src: "gps_off_press.png",
};

export { DEVICE_WIDTH };
