const std = @import("std");
const cli = @import("cli.zig");
const fs = std.fs;

const readme_template =
    \\# {project_name}
    \\
    \\{description}
    \\
    \\## Installation
    \\
    \\```bash
    \\# Add installation instructions here
    \\```
    \\
    \\## Usage
    \\
    \\```bash
    \\# Add usage examples here
    \\```
    \\
    \\## Features
    \\
    \\- Feature 1
    \\- Feature 2
    \\- Feature 3
    \\
    \\## Contributing
    \\
    \\Contributions are welcome! Please feel free to submit a Pull Request.
    \\
    \\## License
    \\
    \\This project is licensed under the MIT License - see the LICENSE file for details.
    \\
;

pub fn initReadmeFn(_options: []const cli.option) bool {
    const cwd_path = fs.cwd().realpathAlloc(std.heap.page_allocator, ".") catch {
        std.debug.print("Error: Could not get current directory\n", .{});
        return false;
    }

    defer std.heap.page_allocator.free(cwd_path);

    const project_name = fs.path.basename(cwd_path);

    if (fs.cwd().access("README.md", .{})) |_| {
        std.debug.print("README.md already exists. Overwrite? (y/n): ", .{});

        const stdin = std.io.getStdIn().reader();
        var buf: [10]u8 = undefined;

        if (stdin.readUntilDelimiterOrEof(&buf, '\n') catch null) |input| {
            if (input.len == 0 or input[0] != 'y' and input[0] != 'Y')) {
                std.debug.print("Aborted.\n", .{});
                return true;
            }
        }
    }else |_| {}

    const file = fs.cwd().createFile("README.md", .{}) catch |err| {
        std.debug.print("Error creating README.md: {}\n", .{err});
        return false;
    }
    defer file.close();

    const writer = file.writer();

    var it = std.mem.tokenize(u8, readme_template, "\n");
    while (it.next()) |line| {
        if (std.mem.indexOf(u8, line, "{project_name}")) |_| {
            var modified_line = std.ArrayList(u8).init(std.heap.page_allocator);
            defer modified_line.deinit();

            var start: usize = 0;
            while (std.mem.indexOf(u8, line[start..], "{project_name}")) |pos| {
                const abs_pos = start + pos;
                modified_line.appendSlice(line[start..abs_pos]) catch return false;
                modified_line.appendSlice(project_name) catch return false;
                start = abs_pos + "{project_name}".len;
            }

            modified_line.appendSlice(line[start..]) catch return false;

            writer.print("{s}\n", .{modified_line.items}) catch return false;
        }else if (std.mem.indexOf(u8, line, "{descrition}")) |_| {
            writer.print("A brief description of {s}\n", .{project_name}) catch return false;
        } else {
            writer.print("{s}\n", .{line}) catch return false;
        }
    }

    std.debug.print("Created README.md for project: {s}\n", .{project_name});
    std.debug.print("Don't forget to update the description and add specific details.\n", .{});

    return true;
}