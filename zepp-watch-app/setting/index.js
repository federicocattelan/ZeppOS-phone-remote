const DEFAULT_SERVER_HOST = "127.0.0.1";
const DEFAULT_SERVER_PORT = "8765";
const DEFAULT_WAZE_PACKAGE = "com.waze";

AppSettingsPage({
  state: {
    authToken: "",
    serverHost: DEFAULT_SERVER_HOST,
    serverPort: DEFAULT_SERVER_PORT,
    wazePackage: DEFAULT_WAZE_PACKAGE,
  },
  build(props) {
    this.loadState(props);

    return View(
      {},
      [
        Text(
          {
            style: {
              margin: "1em 1em 0.5em 1em",
              fontSize: "1.4rem",
              fontWeight: "bold",
            },
          },
          ["Phone Remote"]
        ),
        Text(
          {
            style: { margin: "0 1em 1em 1em", fontSize: "1rem", opacity: 0.8 },
          },
          ["Incolla il token dall'app Android Phone Remote."]
        ),
        Section(
          { title: "Connessione" },
          [
            TextInput({
              label: "Auth token",
              value: this.state.authToken,
              placeholder: "token dall'app Android",
              onChange: (value) => {
                this.state.authToken = value;
              },
            }),
            TextInput({
              label: "Server host",
              value: this.state.serverHost,
              onChange: (value) => {
                this.state.serverHost = value;
              },
            }),
            TextInput({
              label: "Server port",
              value: this.state.serverPort,
              onChange: (value) => {
                this.state.serverPort = value;
              },
            }),
          ]
        ),
        Section(
          { title: "Navigazione" },
          [
            TextInput({
              label: "Package Waze",
              value: this.state.wazePackage,
              placeholder: DEFAULT_WAZE_PACKAGE,
              onChange: (value) => {
                this.state.wazePackage = value;
              },
            }),
          ]
        ),
        Button({
          label: "Salva impostazioni",
          color: "primary",
          style: {
            display: "block",
            margin: "1em",
            width: "auto",
            fontSize: "1.2rem",
          },
          onClick: () => {
            this.saveSettings(props);
          },
        }),
      ]
    );
  },
  loadState(props) {
    this.state.authToken = props.settingsStorage.getItem("authToken") || "";
    this.state.serverHost =
      props.settingsStorage.getItem("serverHost") || DEFAULT_SERVER_HOST;
    this.state.serverPort =
      props.settingsStorage.getItem("serverPort") || DEFAULT_SERVER_PORT;
    this.state.wazePackage =
      props.settingsStorage.getItem("wazePackage") || DEFAULT_WAZE_PACKAGE;
  },
  saveSettings(props) {
    props.settingsStorage.setItem("authToken", this.state.authToken.trim());
    props.settingsStorage.setItem("serverHost", this.state.serverHost.trim());
    props.settingsStorage.setItem("serverPort", this.state.serverPort.trim());
    props.settingsStorage.setItem(
      "wazePackage",
      (this.state.wazePackage || DEFAULT_WAZE_PACKAGE).trim()
    );
  },
});
