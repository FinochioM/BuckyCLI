const std = @import("std");
const cli = @import("cli.zig");
const cmd = @import("commands.zig");
const init_cmd = @import("init_commands.zig");

pub fn main() !void {
    const base_commands = [_]cli.command {
        cli.command {
            .name = "hello",
            .func = &cmd.methods.commands.helloFn,
            .req = &.{"greeting"},
            .opt = &.{"name"},
        },

        // INIT COMMAND WITH SUBCOMMAND
        cli.command {
            .name = "init",
            .func = &init_cmd.initFn,
            .has_subcommand = true,
            .req = &.{"subcommand"},
        },

        // FILE OPERATION
        cli.command {
            .name = "createfile",
            .func = &cmd.methods.commands.createFileFn,
            .req = &.{"filename"},
        },
        cli.command {
            .name = "readfile",
            .func = &cmd.methods.commands.readFileFn,
            .req = &.{"path"},
        },
        cli.command {
            .name = "createfilewithpath",
            .func = &cmd.methods.commands.createFileWithPathFn,
            .req = &.{"path"},
        },
        cli.command {
            .name = "copyfile",
            .func = &cmd.methods.commands.copyFileFn,
            .req = &.{"source", "destination"},
        },
        cli.command {
          .name = "writefile",
          .func = &cmd.methods.commands.writeToFileFn,
          .req = &.{"source", "text"},
        },
    };

    const options = [_]cli.option {
        cli.option{
            .name = "name",
            .short = 'n',
            .long = "name",
            .func = &cmd.methods.options.nameFn,
        },
        cli.option{
            .name = "greeting",
            .short = 'g',
            .long = "greeting",
            .func = &cmd.methods.options.greetingFn,
        },
        cli.option {
            .name = "filename",
            .short = 'f',
            .long = "filename",
            .func = &cmd.methods.options.filenameFn,
        },
        cli.option {
            .name = "path",
            .short = 'p',
            .long = "path",
            .func = &cmd.methods.options.pathFn,
        },
        cli.option {
            .name = "source",
            .short = 's',
            .long = "source",
            .func = &cmd.methods.options.sourceFn,
        },
        cli.option {
            .name = "destination",
            .short = 'd',
            .long = "destination",
            .func = &cmd.methods.options.destinationFn,
        },
        cli.option {
            .name = "text",
            .short = 't',
            .long = "text",
            .func = &cmd.methods.options.textFn,
        },
        cli.option {
            .name = "subcommand",
            .short = 0,
            .long = "subcommand",
            .func = &init_cmd.subcommandFn,
        },
        cli.option {
            .name = "type",
            .short = 0,
            .long = "type",
            .func = &init_cmd.typeFn,
        },
    };

    var commands: [base_commands.len + 1]cli.command = undefined;

    for (base_commands, 0..) |bc, i| {
        commands[i] = bc;
    }

    commands[base_commands.len] = cli.createHelpCommand(&commands, &options);

    try cli.start(&commands, &options, true);
}