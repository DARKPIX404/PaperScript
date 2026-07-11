ps.onEnable(() => {
  ps.logger.info('Hello from a TypeScript plugin!');

  ps.commands.register(
    'hello',
    (ctx) => {
      ctx.sender.sendMessage('Hello, ' + ctx.sender.name + '!');
    },
    'Say hello',
    '/hello'
  );

  ps.events.onPlayerJoin((event) => {
    event.player.sendMessage('Welcome, ' + event.player.name + '!');
  });
});

ps.onDisable(() => {
  ps.logger.info('Hello plugin disabled.');
});
