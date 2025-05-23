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

        pub fn separateCreateFileWithPathFn(path: []const u8) !void {
            if (std.fs.path.dirname(path)) |dir| {
                try fs.cwd().makePath(dir);
            }

            const file = try fs.cwd().createFile(
                path,
                .{},
            );

            defer file.close();

            std.debug.print("File created at: {s}\n", .{path});
        }

        pub fn createFileWithPathFn(_options: []const cli.option) bool {
            var filepath: []const u8 = undefined;

            for (_options) |opt| {
                if (std.mem.eql(u8, opt.name, "path")) {
                    filepath = opt.value;

                    if (separateCreateFileWithPathFn(filepath)) |_| {
                        // success does nothing here.
                    } else |err| {
                        std.debug.print("Error creating the file: {}\n", .{err});
                        return false;
                    }
                }
            }

            return true;
        }

        pub fn separateCopyFileFn(source_path: []const u8, dest_path: []const u8) !void {
            const source_file = try fs.cwd().openFile(source_path, .{});
            defer source_file.close();

            const source_size = try source_file.getEndPos();

            if (std.fs.path.dirname(dest_path)) |dir| {
                try fs.cwd().makePath(dir);
            }

            const dest_file = try fs.cwd().createFile(dest_path, .{});
            defer dest_file.close();

            var buffer: [8192]u8 = undefined;
            var bytes_read: usize = 0;

            while (bytes_read < source_size) {
                const n = try source_file.read(buffer[0..]);
                if (n == 0) break;

                try dest_file.writeAll(buffer[0..n]);
                bytes_read += n;
            }

            std.debug.print("File copied from {s} to {s}\n", .{source_path, dest_path});
        }

        // the source file must exist before the execution but the dest file will be created if it does not exist.
        // if the dest file exists it will just replace its content.
        pub fn copyFileFn(_options: []const cli.option) bool {
                var source_path: []const u8 = undefined;
                var dest_path: []const u8 = undefined;
                var source_found = false;
                var dest_found = false;

                for (_options) |opt| {
                    if (std.mem.eql(u8, opt.name, "source")) {
                        source_path = opt.value;
                        source_found = true;
                    } else if (std.mem.eql(u8, opt.name, "destination")) {
                        dest_path = opt.value;
                        dest_found = true;
                    }
                }

                if (!source_found or !dest_found) {
                    std.debug.print("Both source and destination path are required arguments\n", .{});
                    return false;
                }

                if (separateCopyFileFn(source_path, dest_path)) |_| {
                    // success does nothing here again lol
                } else |err| {
                    std.debug.print("Error copying file: {}\n", .{err});
                    return false;
                }

                return true;
            }

            pub fn separateWriteFileFn(file_path: []const u8, text: []const u8) !void {
                const file = try fs.cwd().openFile(file_path, .{ .mode = .write_only});
                defer file.close();

                try file.writeAll(text);

                std.debug.print("Written the file with the following text: {s}\n", .{text});
            }

            pub fn writeToFileFn(_options: []const cli.option) bool {
                var file_path: []const u8 = undefined;
                var text: []const u8 = undefined;
                var file_exists = false;
                var has_text = false;

                for (_options) |opt| {
                    if (std.mem.eql(u8, opt.name, "source")) {
                        file_path = opt.value;
                        file_exists = true;
                    }else if (std.mem.eql(u8, opt.name, "text")) {
                        text = opt.value;
                        has_text = true;
                    }
                }


                if (!file_exists or !has_text) {
                    std.debug.print("Both 'file path' and 'text' are required arguments", .{});
                    return false;
                }

                if (separateWriteFileFn(file_path, text)) |_| {
                    // success does nothing here again again...
                } else |err| {
                    std.debug.print("Error writing to file: {}\n", .{err});
                    return false;
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
        pub fn sourceFn(_: []const u8) bool {
            return true;
        }
        pub fn destinationFn(_: []const u8) bool {
            return true;
        }
        pub fn textFn(_: []const u8) bool {
            return true;
        }
    };
};