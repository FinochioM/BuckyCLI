const std = @import("std");
const cli = @import("cli.zig");
const cmd = @import("commands.zig");

pub fn main() !void {
    const commands = [_]cli.command {
        cli.command {
            .name = "hello",
            .func = &cmd.methods.commands.helloFn,
            .req = &.{"greeting"},
            .opt = &.{"name"},
        },
        cli.command {
            .name = "help",
            .func = &cmd.methods.commands.helpFn,
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
    };

    try cli.start(&commands, &options, true);
}