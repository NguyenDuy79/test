package com.example.fitness.myapplication.core.base

/**
 * Interface đại diện cho UiState trong mô hình MVI.
 */
interface UiState

/**
 * Interface đại diện cho Intent / Action do người dùng hoặc hệ thống kích hoạt.
 */
interface UiIntent

/**
 * Interface đại diện cho Side Effect (Navigation, Hiển thị thông báo, Dialog...) chỉ kích hoạt 1 lần.
 */
interface UiEffect
