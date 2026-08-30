package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AuditLogDao
import com.example.data.local.dao.CityDao
import com.example.data.local.dao.DriverProfileDao
import com.example.data.local.dao.EmergencyContactDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PricingRuleDao
import com.example.data.local.dao.PromoCodeDao
import com.example.data.local.dao.RideDao
import com.example.data.local.dao.SavedPlaceDao
import com.example.data.local.dao.SupportTicketDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.entity.AuditLogEntity
import com.example.data.local.entity.CityEntity
import com.example.data.local.entity.DriverProfileEntity
import com.example.data.local.entity.EmergencyContactEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.PricingRuleEntity
import com.example.data.local.entity.PromoCodeEntity
import com.example.data.local.entity.RideEntity
import com.example.data.local.entity.SavedPlaceEntity
import com.example.data.local.entity.SupportTicketEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.WalletTransactionEntity

@Database(
    entities = [
        UserEntity::class,
        DriverProfileEntity::class,
        CityEntity::class,
        PricingRuleEntity::class,
        RideEntity::class,
        WalletTransactionEntity::class,
        PromoCodeEntity::class,
        SavedPlaceEntity::class,
        EmergencyContactEntity::class,
        NotificationEntity::class,
        SupportTicketEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RideGoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun driverProfileDao(): DriverProfileDao
    abstract fun cityDao(): CityDao
    abstract fun pricingRuleDao(): PricingRuleDao
    abstract fun rideDao(): RideDao
    abstract fun walletDao(): WalletDao
    abstract fun promoCodeDao(): PromoCodeDao
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun notificationDao(): NotificationDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: RideGoDatabase? = null

        fun getDatabase(context: Context): RideGoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RideGoDatabase::class.java,
                    "ridego_master_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
