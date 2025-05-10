const std = @import("std");
const cli = @import("cli.zig");
const fs  = std.fs;

pub const methods = struct {
    pub const commands = struct {
        pub fn helloFn(_options: []const cli.option) bool {
            var greeting: []const u8 = undefined;
            var name: []const u8 = "World";

            for (_options) |opt| {
                if (std.mem.eql(u8, opt.name, "greeting")) {
                    greeting = opt.value;
                }else if (std.mem.eql(u8, opt.name, "name")) {
                    if (opt.value.len > 0) {
                        name = opt.value;
                    }
                }
            }

            std.debug.print("{s}, {s}!\n", .{greeting, name});
            return true;
        }

        pub fn helpFn(_: []const cli.option) bool {
            std.debug.print(
                "Usage: my-cli <command> [options]\n" ++
                "Commands:\n" ++
                "  hello    Greet someone\n" ++
                "  help     Show this help message\n" ++
                "" ++
                "Options for hello:\n" ++
                "  -n, --name <value>    Name to greet\n" ++
                "  -g, --greetings <value> Change the default 'Hello' greeting message\n"
                , .{}
            );
            return true;
        }

        pub fn separateCreateFileFn(name: []const u8) !void{
            const file = try fs.cwd().createFile(
                name,
                .{}
            );

            defer file.close();
        }

        pub fn createFileFn(_options: []const cli.option) bool{
            var filename: []const u8 = undefined;
            for (_options) |opt| {
                if (std.mem.eql(u8, opt.name, "filename")) {
                    filename = opt.value;

                    const file = separateCreateFileFn(filename);
                    std.debug.print("{any}\n", .{file});
                }
            }

            return true;
        }

        pub fn separateReadFileFn(path: []const u8) !void {
            const file = try fs.cwd().openFile(
                path,
                .{},
            );

            defer file.close();

            var buf_reader = std.io.bufferedReader(file.reader());
            var in_stream = buf_reader.reader();

            var buf: [1024]u8 = undefined;

            while (try in_stream.readUntilDelimiterOrEof(&buf, '\n')) |line| {
                std.debug.print("{s}\n", .{line});
            }
        }

        pub fn readFileFn(_options: []const cli.option) bool {
            var filepath: []const u8 = undefined;

            for (_options) |opt| {
                if (std.mem.eql(u8, opt.name, "path")) {
                    filepath = opt.value;

                    const file = separateReadFileFn(filepath);
                    std.debug.print("{any}\n", .{file});
                }
            }

            return true;
        }
    };

    pub const options = struct {
        pub fn nameFn(_: []const u8) bool {
            return true;
        }
        pub fn greetingFn(_: []const u8) bool {
            return true;
        }
        pub fn filenameFn(_: []const u8) bool {
            return true;
        }
        pub fn pathFn(_: []const u8) bool {
            return true;
        }
    };
};