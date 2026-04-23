package config

import (
	"github.com/spf13/viper"
)

type LDAPConfig struct {
	Host     string `mapstructure:"host"`
	Port     int    `mapstructure:"port"`
	BindDN   string `mapstructure:"bind_dn"`
	Password string `mapstructure:"password"`
	BaseDN   string `mapstructure:"base_dn"`
	UseTLS   bool   `mapstructure:"use_tls"`
}

type Config struct {
	AgentID            string     `mapstructure:"agent_id"`
	AgentToken         string     `mapstructure:"agent_token"`
	BackendURL         string     `mapstructure:"backend_url"`
	CollectIntervalSec int        `mapstructure:"collect_interval_sec"`
	Hostname           string     `mapstructure:"hostname"`
	OSType             string     `mapstructure:"os_type"`
	LDAP               LDAPConfig `mapstructure:"ldap"`
}

func Load() (*Config, error) {
	viper.SetConfigName("agent")
	viper.SetConfigType("yaml")
	viper.AddConfigPath("/etc/ci-defenderx/")
	viper.AddConfigPath(".")

	viper.SetDefault("collect_interval_sec", 30)
	viper.SetDefault("backend_url", "http://localhost:8080")

	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		if _, ok := err.(viper.ConfigFileNotFoundError); !ok {
			return nil, err
		}
	}

	var cfg Config
	if err := viper.Unmarshal(&cfg); err != nil {
		return nil, err
	}
	return &cfg, nil
}
