-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 15, 2024 at 09:27 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `concert_venue_db`
--
CREATE DATABASE IF NOT EXISTS `concert_venue_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `concert_venue_db`;

-- --------------------------------------------------------

--
-- Table structure for table `bookings`
--

DROP TABLE IF EXISTS `bookings`;
CREATE TABLE `bookings` (
  `booking_id` int(11) NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `ticket_type_id` int(11) DEFAULT NULL,
  `booking_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('confirmed','cancelled') DEFAULT 'confirmed'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `bookings`
--

INSERT INTO `bookings` (`booking_id`, `customer_id`, `event_id`, `ticket_type_id`, `booking_date`, `status`) VALUES
(32, 11, 24, 27, '2024-12-15 18:09:35', 'cancelled'),
(33, 11, 24, 28, '2024-12-15 18:26:47', 'cancelled'),
(34, 11, 25, 29, '2024-12-15 18:36:46', 'confirmed'),
(35, 11, 26, 31, '2024-12-15 18:36:50', 'confirmed'),
(36, 11, 26, 31, '2024-12-15 18:36:54', 'confirmed'),
(37, 11, 27, 32, '2024-12-15 18:36:58', 'confirmed'),
(38, 11, 27, 32, '2024-12-15 18:37:01', 'confirmed'),
(39, 11, 27, 32, '2024-12-15 18:37:05', 'confirmed'),
(40, 11, 27, 32, '2024-12-15 18:37:10', 'confirmed'),
(41, 11, 25, 30, '2024-12-15 18:37:14', 'confirmed'),
(42, 11, 25, 30, '2024-12-15 18:37:17', 'confirmed'),
(43, 11, 25, 30, '2024-12-15 18:37:21', 'confirmed');

-- --------------------------------------------------------

--
-- Table structure for table `credit_cards`
--

DROP TABLE IF EXISTS `credit_cards`;
CREATE TABLE `credit_cards` (
  `card_id` int(11) NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `card_holder` varchar(100) DEFAULT NULL,
  `card_number` varchar(16) DEFAULT NULL,
  `expiry_date` varchar(5) DEFAULT NULL,
  `cvv` varchar(3) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `credit_cards`
--

INSERT INTO `credit_cards` (`card_id`, `customer_id`, `card_holder`, `card_number`, `expiry_date`, `cvv`, `status`) VALUES
(9, 11, 'user1', '1234123412341234', '1201', '122', 'active');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `first_name`, `last_name`, `email`, `password_hash`) VALUES
(11, 'User1', 'User11', 'user1@gmail.com', 'XohImNooBHFR0OVvjcYpJ3NgPQ1qq73WKhHvch0VQtg=');

-- --------------------------------------------------------

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
CREATE TABLE `events` (
  `event_id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `event_type` varchar(50) NOT NULL,
  `event_date` date NOT NULL,
  `event_time` time NOT NULL,
  `venue_capacity` int(11) NOT NULL,
  `status` enum('active','cancelled') DEFAULT 'active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `events`
--

INSERT INTO `events` (`event_id`, `name`, `event_type`, `event_date`, `event_time`, `venue_capacity`, `status`) VALUES
(24, 'Event1', 'Concert', '2024-12-28', '20:07:00', 100, 'cancelled'),
(25, 'Event2', 'Concert', '2024-12-18', '20:36:00', 10, 'active'),
(26, 'Event3', 'Musical', '2024-12-27', '08:38:00', 15, 'active'),
(27, 'Event4', 'Concert', '2025-05-22', '08:42:00', 100, 'active');

-- --------------------------------------------------------

--
-- Stand-in structure for view `event_details_view`
-- (See below for the actual view)
--
DROP VIEW IF EXISTS `event_details_view`;
CREATE TABLE `event_details_view` (
`event_id` int(11)
,`name` varchar(100)
,`event_type` varchar(50)
,`event_date` date
,`event_time` time
,`venue_capacity` int(11)
,`status` enum('active','cancelled')
,`type_id` int(11)
,`type_name` varchar(50)
,`price` decimal(10,2)
,`quantity_available` int(11)
,`revenue` decimal(32,2)
);

-- --------------------------------------------------------

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
  `payment_id` int(11) NOT NULL,
  `booking_id` int(11) DEFAULT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `payment_date` datetime DEFAULT NULL,
  `card_id` int(11) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `payments`
--

INSERT INTO `payments` (`payment_id`, `booking_id`, `customer_id`, `amount`, `payment_date`, `card_id`, `status`) VALUES
(36, 32, 11, 10.00, '2024-12-15 20:09:35', 9, '1'),
(37, 32, 11, 10.00, '2024-12-15 20:11:25', 9, '0'),
(38, 33, 11, 20.00, '2024-12-15 20:26:47', 9, '1'),
(39, 33, 11, 20.00, '2024-12-15 20:28:05', 9, '0'),
(40, 34, 11, 5.00, '2024-12-15 20:36:46', 9, '1'),
(41, 35, 11, 15.00, '2024-12-15 20:36:50', 9, '1'),
(42, 36, 11, 15.00, '2024-12-15 20:36:54', 9, '1'),
(43, 37, 11, 5.00, '2024-12-15 20:36:58', 9, '1'),
(44, 38, 11, 5.00, '2024-12-15 20:37:01', 9, '1'),
(45, 39, 11, 5.00, '2024-12-15 20:37:05', 9, '1'),
(46, 40, 11, 5.00, '2024-12-15 20:37:10', 9, '1'),
(47, 41, 11, 10.00, '2024-12-15 20:37:14', 9, '1'),
(48, 42, 11, 10.00, '2024-12-15 20:37:17', 9, '1'),
(49, 43, 11, 10.00, '2024-12-15 20:37:21', 9, '1');

-- --------------------------------------------------------

--
-- Stand-in structure for view `payment_statistics_view`
-- (See below for the actual view)
--
DROP VIEW IF EXISTS `payment_statistics_view`;
CREATE TABLE `payment_statistics_view` (
`payment_id` int(11)
,`booking_id` int(11)
,`customer_id` int(11)
,`amount` decimal(10,2)
,`payment_date` datetime
,`status` varchar(20)
,`event_id` int(11)
,`event_name` varchar(100)
,`booking_status` enum('confirmed','cancelled')
,`customer_name` varchar(101)
,`card_number` varchar(16)
,`type_id` int(11)
,`type_name` varchar(50)
,`price` decimal(10,2)
,`event_total_revenue` decimal(32,2)
,`event_total_bookings` bigint(21)
,`ticket_type_revenue` decimal(32,2)
);

-- --------------------------------------------------------

--
-- Table structure for table `ticket_types`
--

DROP TABLE IF EXISTS `ticket_types`;
CREATE TABLE `ticket_types` (
  `type_id` int(11) NOT NULL,
  `event_id` int(11) DEFAULT NULL,
  `type_name` varchar(50) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `quantity_available` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ticket_types`
--

INSERT INTO `ticket_types` (`type_id`, `event_id`, `type_name`, `price`, `quantity_available`) VALUES
(27, 24, 'type1', 10.00, 90),
(28, 24, 'type2', 20.00, 9),
(29, 25, '1', 5.00, 4),
(30, 25, '2', 10.00, 2),
(31, 26, '11', 15.00, 13),
(32, 27, 'standard', 5.00, 96);

-- --------------------------------------------------------

--
-- Structure for view `event_details_view`
--
DROP TABLE IF EXISTS `event_details_view`;

DROP VIEW IF EXISTS `event_details_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `event_details_view`  AS SELECT `e`.`event_id` AS `event_id`, `e`.`name` AS `name`, `e`.`event_type` AS `event_type`, `e`.`event_date` AS `event_date`, `e`.`event_time` AS `event_time`, `e`.`venue_capacity` AS `venue_capacity`, `e`.`status` AS `status`, `t`.`type_id` AS `type_id`, `t`.`type_name` AS `type_name`, `t`.`price` AS `price`, `t`.`quantity_available` AS `quantity_available`, coalesce((select sum(case when `p`.`status` = 1 then `p`.`amount` when `p`.`status` = 0 then -`p`.`amount` else 0 end) from (`bookings` `b` join `payments` `p` on(`b`.`booking_id` = `p`.`booking_id`)) where `b`.`ticket_type_id` = `t`.`type_id`),0) AS `revenue` FROM (`events` `e` left join `ticket_types` `t` on(`e`.`event_id` = `t`.`event_id`)) GROUP BY `e`.`event_id`, `t`.`type_id` ;

-- --------------------------------------------------------

--
-- Structure for view `payment_statistics_view`
--
DROP TABLE IF EXISTS `payment_statistics_view`;

DROP VIEW IF EXISTS `payment_statistics_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `payment_statistics_view`  AS SELECT `p`.`payment_id` AS `payment_id`, `p`.`booking_id` AS `booking_id`, `p`.`customer_id` AS `customer_id`, `p`.`amount` AS `amount`, `p`.`payment_date` AS `payment_date`, `p`.`status` AS `status`, `e`.`event_id` AS `event_id`, `e`.`name` AS `event_name`, `b`.`status` AS `booking_status`, concat(`c`.`first_name`,' ',`c`.`last_name`) AS `customer_name`, `cc`.`card_number` AS `card_number`, `tt`.`type_id` AS `type_id`, `tt`.`type_name` AS `type_name`, `tt`.`price` AS `price`, sum(case when `p`.`status` = 1 then `p`.`amount` else 0 end) over ( partition by `e`.`event_id`) AS `event_total_revenue`, count(case when `p`.`status` = 1 then 1 end) over ( partition by `e`.`event_id`) AS `event_total_bookings`, sum(case when `p`.`status` = 1 then `p`.`amount` else 0 end) over ( partition by `e`.`event_id`,`tt`.`type_name`) AS `ticket_type_revenue` FROM (((((`payments` `p` join `bookings` `b` on(`p`.`booking_id` = `b`.`booking_id`)) join `events` `e` on(`b`.`event_id` = `e`.`event_id`)) join `customers` `c` on(`p`.`customer_id` = `c`.`customer_id`)) join `credit_cards` `cc` on(`p`.`card_id` = `cc`.`card_id`)) join `ticket_types` `tt` on(`b`.`ticket_type_id` = `tt`.`type_id`)) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`booking_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `event_id` (`event_id`),
  ADD KEY `ticket_type_id` (`ticket_type_id`);

--
-- Indexes for table `credit_cards`
--
ALTER TABLE `credit_cards`
  ADD PRIMARY KEY (`card_id`),
  ADD KEY `customer_id` (`customer_id`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `events`
--
ALTER TABLE `events`
  ADD PRIMARY KEY (`event_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`payment_id`),
  ADD KEY `booking_id` (`booking_id`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `card_id` (`card_id`);

--
-- Indexes for table `ticket_types`
--
ALTER TABLE `ticket_types`
  ADD PRIMARY KEY (`type_id`),
  ADD KEY `event_id` (`event_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bookings`
--
ALTER TABLE `bookings`
  MODIFY `booking_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=44;

--
-- AUTO_INCREMENT for table `credit_cards`
--
ALTER TABLE `credit_cards`
  MODIFY `card_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `events`
--
ALTER TABLE `events`
  MODIFY `event_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT for table `payments`
--
ALTER TABLE `payments`
  MODIFY `payment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=50;

--
-- AUTO_INCREMENT for table `ticket_types`
--
ALTER TABLE `ticket_types`
  MODIFY `type_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `bookings_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`),
  ADD CONSTRAINT `bookings_ibfk_2` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`),
  ADD CONSTRAINT `bookings_ibfk_3` FOREIGN KEY (`ticket_type_id`) REFERENCES `ticket_types` (`type_id`);

--
-- Constraints for table `credit_cards`
--
ALTER TABLE `credit_cards`
  ADD CONSTRAINT `credit_cards_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`);

--
-- Constraints for table `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `payments_ibfk_1` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`booking_id`),
  ADD CONSTRAINT `payments_ibfk_2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`),
  ADD CONSTRAINT `payments_ibfk_3` FOREIGN KEY (`card_id`) REFERENCES `credit_cards` (`card_id`);

--
-- Constraints for table `ticket_types`
--
ALTER TABLE `ticket_types`
  ADD CONSTRAINT `ticket_types_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `events` (`event_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
