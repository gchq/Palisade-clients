<!---
Copyright 2018-2021 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->

# Palisade FuseFS Client

## Introduction

> ***Filesystem in Userspace (FUSE)*** is a software interface for Unix and Unix-like computer operating systems that lets non-privileged users create their own file systems without editing kernel code.
This is achieved by running file system code in user space while the FUSE module provides only a "bridge" to the actual kernel interfaces.
[[1]](https://en.wikipedia.org/wiki/Filesystem_in_Userspace)

Within the context of Palisade, this allows us to make a request with `userId`, `resourceId` and `context`, then create a software-controlled filesystem mount to represent the returned resources and data.
The Filtered-Resource Service response represents a directory listing (we must convert it into a tree and perform some simple processing).
The Data Service read represents a file read.


## Requirements
Depending on operating system, you will need an appropriate FUSE implementation.

The following are the most popular libraries for the three major OSes:
* [libfuse for Linux and BSD](https://github.com/libfuse/libfuse)
    * [libfuse3-dev for Ubuntu Hirsute](https://packages.ubuntu.com/hirsute/libfuse3-dev)
    * [libfuse3-dev for Debian Sid](https://packages.debian.org/sid/libfuse3-dev)
    * [fuse2 for Arch (x86-64)](https://archlinux.org/packages/extra/x86_64/fuse2/)
* [OSXFUSE for MacOS](https://osxfuse.github.io/)
* [WinFSP for Windows](http://www.secfs.net/winfsp/)


## Usage

Register a request to Palisade and the `<mountDir>` will be populated with resources.
```shell script
java -jar client-fuse.jar <clientUri> <resourceId> <mountDir>
```
See the [URL configuration](../client-java/README.md#URL) and [client properties](../client-java/README.md#Client%20properties) sections of the [client-java README](../client-java/README.md) for details on the `<clientUri>`.


## Notes

In terms of ease-of-use and approachability, FUSE is an easy way to get started with Palisade.
All existing tools for working with UNIX filesystems and directory trees will work without any changes required.

Despite this, this is not particularly flexible (no option for exposing additional resource metadata), memory-efficient (the full tree of returned resources must be stored), or scalable (not appropriate for a distributed map-reduce scenario).
A 'power-user' may prefer working directly with the [Java Client](../client-java/README.md).
