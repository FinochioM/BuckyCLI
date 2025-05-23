const std = @import("std");
const builtin = @import("builtin");

pub const MAX_COMMANDS: u8 = 20;
pub const MAX_OPTIONS: u8 = 20;

const Byte = u8;
const Slice = []const Byte;
const Slices = []const Slice;

pub const command = struct {
    name: Slice,
    func: fnType,
    req: Slices = &.{},
    opt: Slices = &.{},
    const fnType = *const fn([]const option) bool;
};

pub const option = struct {
    name: Slice,
    func: ?fnType = null,
    short: Byte,
    long: Slice,
    value: Slice = "",
    const fnType = *const fn(Slice) bool;
};

pub const Error = error {
    NoArgsProvided,
    UnknownCommand,
    UnknownOption,
    MissingRequiredOption,
    UnexpectedArgument,
    CommandExecutionFailed,
    TooManyCommands,
    TooManyOptions,
};

pub fn generateHelp(commands: []const command, options: []const option) void {
    std.debug.print("Usage: <command> [options]\n\nCommands:\n", .{});

    for (commands) |cmd| {
        std.debug.print(" {s<:15} ", .{cmd.name});
        if (cmd.req.len > 0) {
            std.debug.print("Required: ", .{});
            for (cmd.req, 0..) |req, i| {
                if (i > 0) std.debug.print(", ", .{});
                std.debug.print("--{s}", .{req});
            }
        }

        if (cmd.opt.len > 0) {
            if (cmd.req.len > 0) std.debug.print(" | ", .{});
            std.debug.print("Optional: ", .{});
            for (cmd.opt, 0..) |opt, i| {
                if (i > 0) std.debug.print(", ", .{});
                std.debug.print("--{s}", .{opt});
            }
        }

        std.debug.print("\n", .{});
    }

    std.debug.print("\nOptions:\n", .{});
    for (options) |opt| {
        std.debug.print("   -{c}, --{s:<12} {s}\n", .{opt.short, opt.long, opt.name});
    }
}

pub fn createHelpCommand(commands: []const command, options: []const option) command {
    const HelpContext = struct {
        var cmds: []const command = undefined;
        var opts: []const option = undefined;

        pub fn helpFn(_: []const option) bool {
            generateHelp(cmds, opts);
            return true;
        }
    };

    HelpContext.cmds = commands;
    HelpContext.opts = options;

    return command {
        .name = "help",
        .func = &HelpContext.helpFn,
    };
}

pub fn start(commands: []const command, options: []const option, debug: bool) !void{
    if (commands.len > MAX_COMMANDS) {
        return Error.TooManyCommands;
    }

    if (options.len > MAX_OPTIONS) {
        return Error.TooManyOptions;
    }

    var gpa = std.heap.GeneralPurposeAllocator(.{}) {};
    defer _ = gpa.deinit();
    const allocator = gpa.allocator();

    const args = try std.process.argsAlloc(allocator);
    defer std.process.argsFree(allocator, args);

    try startWithArgs(commands, options, args, debug);
}

pub fn startWithArgs(commands: []const command, options: []const option, args: anytype, debug: bool) !void {
    if (args.len < 2) {
        if (debug) std.debug.print("No command provided by user!\n", .{});
        return Error.NoArgsProvided;
    }

    const command_name = args[1];
    var detected_command: ?command = null;

    for (commands) |cmd| {
        if (std.mem.eql(u8, cmd.name, command_name)) {
            detected_command = cmd;
            break;
        }
    }

    if (detected_command == null) {
        if (debug) std.debug.print("Unknown command: {s}n", .{command_name});
        return Error.UnknownCommand;
    }

    const cmd = detected_command.?;

    if (debug) std.debug.print("Detected command: {s}\n", .{cmd.name});

    var detected_options: [MAX_OPTIONS]option = undefined;
    var detected_len: usize = 0;
    var i: usize = 2;

    while (i < args.len) {
        const arg = args[i];

        if (std.mem.startsWith(u8, arg, "-")) {
            const option_name = if (std.mem.startsWith(u8, arg[1..], "-")) arg[2..] else arg[1..];
            var matched_option: ?option = null;

            for (options) |opt| {
                if (std.mem.eql(u8, option_name, opt.long) or (option_name.len == 1 and option_name[0] == opt.short)) {
                    matched_option = opt;
                    break;
                }
            }

            if (matched_option == null) {
                if (debug) std.debug.print("Unknown option: {s}\n", .{arg});
                return Error.UnknownOption;
            }

            var opt = matched_option.?;

            if (i + 1 < args.len and !std.mem.startsWith(u8, args[i + 1], "-")) {
                opt.value = args[i + 1];
                i += 1;
            }else {
                opt.value = "";
            }

            if (detected_len >= MAX_OPTIONS) {
                return Error.TooManyOptions;
            }

            detected_options[detected_len] = opt;
            detected_len += 1;
        }else {
            if (debug) std.debug.print("Unexpected argument: {s}\n", .{arg});
            return Error.UnexpectedArgument;
        }

        i += 1;
    }

    const used_options = detected_options[0..detected_len];

    for (cmd.req) |req_option| {
        var found = false;

        for (used_options) |opt| {
            if (std.mem.eql(u8, req_option, opt.name)) {
                found = true; break;
            }
        }

        if (!found) {
            if (debug) std.debug.print("Missing required option: {s}\n", .{req_option});
            return error.RequiredMissingOption;
        }
    }

    if (!cmd.func(used_options)) {
        return Error.CommandExecutionFailed;
    }else {
        for (used_options) |opt| {
            if (opt.func == null) continue;

            const result = opt.func.?(opt.value);

            if (!result) {
                if (debug) std.debug.print("Option function execution failed: {s}\n", .{opt.name});
                return Error.CommandExecutionFailed;
            }
        }
    }

    if (debug) std.debug.print("Command executed succesfully: {s}\n", .{cmd.name});
}

pub const Color = enum {
    Reset,
    Black,
    Red,
    Green,
    Yellow,
    Blue,
    Magenta,
    Cyan,
    White,
};

