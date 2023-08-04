#!/usr/bin/env python3

# Helper script to calculate reboot_after_timeout_values

collect_e = ["off"]
collect_v = [0]

def minutes(x):
    return 60*1000*x

def hours(x):
    return 60*minutes(x)

def show(x, fun):
    v = fun(x)
    print(f"{x} {fun.__name__} -> {v}")
    collect_v.append(v)
    collect_e.append(f"{fun.__name__}_{x}")

show(10, minutes)
show(30, minutes)
show(1, hours)
show(2, hours)
show(4, hours)
show(8, hours)
show(12, hours)
show(24, hours)
show(36, hours)
show(48, hours)
show(72, hours)

print("Entries:")
for e in collect_e:
    print(f"        <item>@string/reboot_after_timeout_{e}</item>")

print("Values:")
for v in collect_v:
    print(f"        <item>{v}</item>")
