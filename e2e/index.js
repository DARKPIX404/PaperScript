// E2E probe for the PaperScript host. Loaded by the host inside a real Paper server.
ps.commands.register("psping", function () {
  ps.logger.info("PS_E2E_PONG");
});
ps.logger.info("PS_E2E_READY");
