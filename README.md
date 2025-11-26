# Robotic Vacuum Cleaner

## Overview
A concurrent Microservices system simulating a robotic vacuum cleaner. The system is designed to perform real-time environment mapping using simulated sensor data.

This project demonstrates advanced Java Concurrency concepts and architectural patterns, focusing on thread safety and scalable system design.

## Key Features
* **Microservices Architecture:** Decomposed the system into independent services communicating via a central bus.
* **Custom MessageBus:** Implemented a thread-safe Publish-Subscribe mechanism with Round-Robin scheduling to manage event flow.
* **Concurrency & Synchronization:** Utilized Java’s Future and synchronization primitives to handle asynchronous tasks safely.
* **SLAM Algorithms:** Executed sensor data fusion and Simultaneous Localization and Mapping (SLAM) for accurate room mapping.

## Tech Stack
* **Language:** Java
* **Concepts:** Multithreading, Thread-Safety, Event-Driven Architecture, Object-Oriented Design.

---
Created as part of the Systems Programming Laboratory course.
