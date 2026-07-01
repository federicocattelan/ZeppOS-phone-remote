import { BaseSideService } from "@zeppos/zml/base-side";
import {
  DEFAULT_SERVER_HOST,
  DEFAULT_SERVER_PORT,
  DEFAULT_WAZE_PACKAGE,
} from "../utils/shortcuts";

function parseBody(res) {
  if (!res || res.body === undefined || res.body === null) {
    return {};
  }

  return typeof res.body === "string" ? JSON.parse(res.body) : res.body;
}

function getServerConfig() {
  const host =
    settings.settingsStorage.getItem("serverHost") || DEFAULT_SERVER_HOST;
  const port =
    settings.settingsStorage.getItem("serverPort") || DEFAULT_SERVER_PORT;
  const token = settings.settingsStorage.getItem("authToken") || "";

  return {
    baseUrl: `http://${host}:${port}`,
    token,
  };
}

async function companionRequest(path, method, body) {
  const { baseUrl, token } = getServerConfig();

  if (!token) {
    return {
      success: false,
      message: "Auth token missing. Configure it in the Zepp app settings.",
    };
  }

  const options = {
    url: `${baseUrl}${path}`,
    method,
    headers: {
      "Content-Type": "application/json",
      "X-Auth-Token": token,
    },
  };

  if (body !== undefined) {
    options.body = JSON.stringify(body);
  }

  const res = await fetch(options);
  return parseBody(res);
}

async function executeShortcut(params) {
  const { action, package: packageName } = params;

  if (action === "gps_on" || action === "gps_off") {
    return companionRequest("/api/gps", "POST", {
      enabled: action === "gps_on",
    });
  }

  if (action === "launch_waze") {
    const wazePackage =
      settings.settingsStorage.getItem("wazePackage") || DEFAULT_WAZE_PACKAGE;
    return companionRequest("/api/launch", "POST", {
      package: wazePackage,
    });
  }

  if (action === "launch") {
    return companionRequest("/api/launch", "POST", {
      package: packageName,
    });
  }

  return {
    success: false,
    message: `Unknown action: ${action}`,
  };
}

AppSideService(
  BaseSideService({
    onInit() {
      console.log("Phone Remote side service ready");
    },
    async onRequest(req, res) {
      const { method, params = {} } = req;

      try {
        if (method === "EXECUTE_SHORTCUT") {
          const result = await executeShortcut(params);
          res(null, result);
          return;
        }

        if (method === "HEALTH_CHECK") {
          const { baseUrl } = getServerConfig();
          const healthRes = await fetch({
            url: `${baseUrl}/health`,
            method: "GET",
          });
          res(null, parseBody(healthRes));
          return;
        }

        res("Unknown request method", null);
      } catch (error) {
        console.log(error);
        res(null, {
          success: false,
          message: "Companion app unreachable. Open Phone Remote on the phone.",
        });
      }
    },
  })
);
